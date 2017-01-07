package api.data;

import api.Main;
import api.deployer.DeployerServer;
import api.packets.MessengerClient;
import api.packets.server.ServerStatePacket;
import api.utils.SimpleManager;
import api.utils.Utils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by SkyBeast on 19/12/2016.
 */
public class DataManager implements SimpleManager
{
	private static final Document EMPTY_DOCUMENT = new Document();
	private final long saveDelay;
	private final Listen listener = new Listen();
	private final MongoDatabase usersDB;
	private final ExecutorService exec = Executors.newCachedThreadPool();
	private final List<UserData> users = new ArrayList<>();
	private final ReentrantLock usersLock = new ReentrantLock();
	private final List<Server> servers = new ArrayList<>();
	private final ReentrantLock serversLock = new ReentrantLock();
	private final DelayQueue<UserData.Delay> disconnectQueue = new DelayQueue<>();
	private final List<UUID> uuids = new ArrayList<>();
	private final AtomicInteger severCount = new AtomicInteger();
	private boolean init = false;
	private volatile boolean end = false;
	private Thread saverThread;

	public DataManager(long saveDelay)
	{
		this.saveDelay = saveDelay;
		usersDB = Main.getInstance().getMongoClient().getDatabase("users");
	}

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialised!");

		setupSaverThread();

		ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), listener);

		init = true;
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");

		saverThread.interrupt();

		end = true;
		Main.getInstance().getLogger().info(this + " stopped.");
	}

	private void setupSaverThread()
	{
		saverThread = new Thread(() ->
		{
			while (!end)
			{
				try (Jedis jedis = Main.getInstance().getJedisPool().getResource())
				{
					UserData.Delay delay = disconnectQueue.take();
					UserData user = delay.parent();

					String prefix = user.getRedisPrefix();

					//Get from Redis

					Transaction transaction = jedis.multi();
					Response<String> rFC = transaction.get(prefix + "fc");
					Response<String> rTC = transaction.get(prefix + "tc");
					Response<String> rState = transaction.get(prefix + "state");
					transaction.exec();

					int fc = Utils.stringToInt(rFC.get());
					int tc = Utils.stringToInt(rTC.get());
					int state = Utils.stringToInt(rState.get());

					//Save to MongoDB

					MongoCollection<Document> col = usersDB.getCollection(user.getBase64UUID());
					Document doc = col.find().first();
					doc.put("fc", fc);
					doc.put("tc", tc);
					doc.put("state", state);
					col.replaceOne(EMPTY_DOCUMENT, doc);

					//Remove from Redis

					jedis.del(prefix + "fc", prefix + "tc", prefix + "rank", prefix + "party", prefix + "friends",
							prefix + "state", prefix + "warn");
				}
				catch (InterruptedException e)
				{
					if (!end)
						Main.getInstance().getLogger().log(Level.SEVERE, "Error while saving to MongoDB " +
								"(DataManager: " + this + ")", e);
				}
				catch (Exception e)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "Error while saving to MongoDB " +
							"(DataManager: " + this + ")", e);
				}
			}
		}
		);

		saverThread.start();
	}

	public Server findServerByPort(int port)
	{
		serversLock.lock();
		try
		{
			return servers.stream()
					.filter(srv -> srv.getDeployer().getPort() == port)
					.findAny().orElse(null);
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public UserData getData(ProxiedPlayer player)
	{
		usersLock.lock();
		try
		{
			return users.stream()
					.filter(userData -> userData.getPlayer().equals(player))
					.findFirst().orElse(null);
		}
		finally
		{
			usersLock.unlock();
		}
	}

	public UserData getOnline(OfflineUserData player)
	{
		usersLock.lock();
		try
		{
			return users.stream()
					.filter(userData -> userData.getUUID().equals(player.getUUID()))
					.findFirst().orElse(null);
		}
		finally
		{
			usersLock.unlock();
		}
	}

	public void forEachServers(Consumer<Server> cons)
	{
		serversLock.lock();
		try
		{
			servers.forEach(cons);
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public List<Server> getServersByType(DeployerServer.ServerType type)
	{
		serversLock.lock();
		try
		{
			return servers.stream()
					.filter(server -> server.getDeployer().getType().equals(type))
					.collect(Collectors.toList());
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public void forEachUsers(Consumer<UserData> consumer)
	{
		usersLock.lock();
		try
		{
			users.forEach(consumer);
		}
		finally
		{
			usersLock.unlock();
		}
	}

	public int getNextDeployerID(int maxServers)
	{
		serversLock.lock();
		try
		{
			List<Integer> ports = servers.stream().map(server -> server.getDeployer().getOffset()).collect(Collectors
					.toList());
			return Stream.iterate(0, id -> id + 1).limit(maxServers)
					.filter(i -> !ports.contains(i))
					.findFirst().orElse(-1);
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public int getNextDeployerPort(int minPort, int maxPort)
	{
		serversLock.lock();
		try
		{
			List<Integer> ports = servers.stream()
					.map(server -> server.getDeployer().getPort())
					.collect(Collectors.toList());

			return Stream.iterate(minPort, port -> port + 1).limit(maxPort)
					.filter(i -> !ports.contains(i))
					.findFirst().orElse(-1);
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public Server constructServer(DeployerServer deployer, ServerInfo info)
	{
		severCount.getAndIncrement();

		serversLock.lock();
		try
		{
			Server server = new Server(deployer, info);
			servers.add(server);
			uuids.add(deployer.getServerUUID());
			return server;
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public Server getServer(ServerInfo info)
	{
		serversLock.lock();
		try
		{
			return servers.stream().filter(server -> server.getInfo().equals(info))
					.findFirst().orElse(null);
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public Server getServer(UUID uuid)
	{
		serversLock.lock();
		try
		{
			return servers.stream().filter(server -> server.getUUID().equals(uuid))
					.findFirst().orElse(null);
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public Server getServer(String base64UUID)
	{
		serversLock.lock();
		try
		{
			return servers.stream().filter(server -> server.getBase64UUID().equalsIgnoreCase(base64UUID))
					.findFirst().orElse(null);
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public void unregisterServer(Server server)
	{
		serversLock.lock();
		try
		{
			servers.remove(server);
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public int getServerCount()
	{
		return severCount.get();
	}

	public UUID newUUID()
	{
		UUID uuid;
		do uuid = UUID.randomUUID();
		while (uuids.contains(uuid));

		return uuid;
	}

	public void updateMessenger(Server srv, MessengerClient client)
	{
		srv.setMessenger(client);
	}

	public void updateServerState(Server srv, ServerStatePacket.ServerState state)
	{
		srv.setServerState(state);
	}

	public void updateLastKeepAlive(Server srv, long lastKeepAlive)
	{
		srv.setLastKeepAlive(lastKeepAlive);
	}

	@Override
	public String toString()
	{
		return "DataManager{" +
				"saveDelay=" + saveDelay +
				", init=" + init +
				", end=" + end +
				", listener=" + listener +
				", usersDB=" + usersDB +
				", exec=" + exec +
				", users=" + users +
				", usersLock=" + usersLock +
				", servers=" + servers +
				", serversLock=" + serversLock +
				", disconnectQueue=" + disconnectQueue +
				", uuids=" + uuids +
				", severCount=" + severCount +
				", saverThread=" + saverThread +
				'}';
	}

	public class Listen implements Listener
	{
		private Listen() {}

		@EventHandler
		public void onServerSwitch(ServerConnectEvent event)
		{
			exec.submit(() ->
					{
						try (Jedis jedis = Main.getInstance().getJedisPool().getResource())
						{
							UserData data = getData(event.getPlayer());

							Transaction tr1 = jedis.multi();
							Response<String> rank = tr1.get(data.getRedisPrefix() + ":rank");
							tr1.set(data.getRedisPrefix() + ":srv", event.getTarget().getName());
							tr1.exec();
						}
					}
			);
		}

		@EventHandler
		public void onJoin(PostLoginEvent event)
		{
			exec.submit(() ->
					{
						try (Jedis jedis = Main.getInstance().getJedisPool().getResource())
						{
							ProxiedPlayer player = event.getPlayer();

							//Get data if cached

							Iterator<UserData.Delay> ite = disconnectQueue.iterator();
							for (UserData.Delay delay = ite.next(); ite.hasNext(); )
							{
								UserData data = delay.parent();
								if (data.getPlayer().getUniqueId().equals(player.getUniqueId())) // Player
								// already cached in Redis
								{
									ite.remove();

									usersLock.lock();
									try
									{
										users.add(data);
									}
									finally
									{
										usersLock.unlock();
									}

									return;
								}
							}

							//Else, create data, read in MongoDB then send to Redis

							String base64 = Utils.formatToUUID(player.getUniqueId());
							UserData data = new UserData(player, base64);

							MongoCollection<Document> col = usersDB.getCollection(base64);
							Document doc = col.find().first();
							if (doc == null)
							{
								doc = new Document();
								doc.put("firstJoin", System.currentTimeMillis()); // NEWBIE
							}

							int fc = doc.getInteger("fc", 0);
							int tc = doc.getInteger("tc", 0);
							int rank = doc.getInteger("rank", 0);
							int state = doc.getInteger("state", 0);

							Transaction transaction = jedis.multi();
							transaction.set("fc", Utils.intToString(fc));
							transaction.set("tc", Utils.intToString(tc));
							transaction.set("rank", Utils.intToString(rank));
							transaction.set("state", Utils.intToString(state));
							transaction.exec();
						}
					}
			);
		}

		@EventHandler
		public void onQuit(PlayerDisconnectEvent event)
		{
			UserData data = getData(event.getPlayer());
			if (data == null)
				return;
			UserData.Delay delay = data.getDelayer();
			delay.deadLine = saveDelay + System.currentTimeMillis();
			disconnectQueue.add(delay);
		}
	}
}
