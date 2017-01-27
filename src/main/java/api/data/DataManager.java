package api.data;

import api.Main;
import api.deployer.DeployerServer;
import api.events.ServerChangeStateEvent;
import api.packets.MessengerClient;
import api.packets.server.ServerStatePacket;
import api.utils.SimpleManager;
import api.utils.Utils;
import api.utils.concurrent.ThreadLoop;
import api.utils.concurrent.ThreadLoops;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.ToString;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by SkyBeast on 19/12/2016.
 */
@ToString
public final class DataManager implements SimpleManager
{
	private static final Document EMPTY_DOCUMENT = new Document();
	private static final long SAVE_DELAY = 3 * 60 * 1000;
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
	private boolean init;
	private volatile boolean end;
	private final ThreadLoop saverThread = setupSaverThread();

	public DataManager()
	{
		usersDB = Main.getInstance().getMongoClient().getDatabase("users");
	}

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialised!");

		saverThread.start();

		ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), listener);

		init = true;
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");

		saverThread.stop();

		end = true;
		Main.getInstance().getLogger().info(this + " stopped.");
	}

	private ThreadLoop setupSaverThread()
	{
		return ThreadLoops.newInfiniteThreadLoop
				(
						() ->
						{
							System.out.println("PreTake");
							UserData.Delay delay = disconnectQueue.take();
							try (Jedis jedis = Main.getInstance().getJedisPool().getResource())
							{
								System.out.println("PostTake");
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

								jedis.del(prefix + "fc", prefix + "tc", prefix + "rank", prefix + "party", prefix +
												"friends",
										prefix + "state", prefix + "warn");
							}
						}
				);
	}

	public Server findServerByPort(int port)
	{
		return Utils.doLocked
				(
						() -> servers.stream()
								.filter(srv -> srv.getDeployer().getPort() == port)
								.findAny().orElse(null),
						serversLock
				);
	}

	public UserData getData(ProxiedPlayer player)
	{
		return Utils.doLocked
				(
						() -> users.stream()
								.filter(userData -> userData.getPlayer().equals(player))
								.findFirst().orElse(null),
						serversLock
				);
	}

	public UserData getOnline(OfflineUserData player)
	{
		return Utils.doLocked
				(
						() -> users.stream()
								.filter(userData -> userData.getUuid().equals(player.getUuid()))
								.findFirst().orElse(null),
						serversLock
				);
	}

	public void forEachServers(Consumer<? super Server> cons)
	{
		Utils.doLocked
				(
						() -> servers.forEach(cons),
						serversLock
				);
	}

	public List<Server> getServersByType(DeployerServer.ServerType type)
	{
		return Utils.doLocked
				(
						() -> servers.stream()
								.filter(server -> server.getDeployer().getType() == type)
								.collect(Collectors.toList()),
						serversLock
				);
	}

	public void forEachUsers(Consumer<? super UserData> consumer)
	{
		Utils.doLocked
				(
						() -> users.forEach(consumer),
						usersLock
				);
	}

	public int getNextDeployerID(int maxServers)
	{
		return Utils.doLocked
				(
						() ->
						{
							List<Integer> ports = servers.stream()
									.map(server -> server.getDeployer().getOffset())
									.collect(Collectors.toList());
							return Stream.iterate(0, id -> id + 1)
									.limit(maxServers)
									.filter(i -> !ports.contains(i))
									.findFirst().orElse(-1);
						},
						serversLock
				);
	}

	public int getNextDeployerPort(int minPort, int maxPort)
	{
		return Utils.doLocked
				(
						() ->
						{
							List<Integer> ports = servers.stream()
									.map(server -> server.getDeployer().getPort())
									.collect(Collectors.toList());
							return Stream.iterate(minPort, port -> port + 1)
									.limit(maxPort)
									.filter(i -> !ports.contains(i))
									.findFirst().orElse(-1);
						},
						serversLock
				);
	}

	public Server constructServer(DeployerServer deployer, ServerInfo info)
	{
		severCount.getAndIncrement();

		return Utils.doLocked
				(
						() ->
						{
							Server server = new Server(deployer, info);
							servers.add(server);
							uuids.add(deployer.getUuid());
							return server;
						},
						serversLock
				);
	}

	public Server getServer(ServerInfo info)
	{
		severCount.getAndIncrement();

		return Utils.doLocked
				(
						() -> servers.stream()
								.filter(server -> server.getInfo().equals(info))
								.findFirst()
								.orElse(null),
						serversLock
				);
	}

	public Server getServer(UUID uuid)
	{
		return Utils.doLocked
				(
						() -> servers.stream()
								.filter(server -> server.getUuid().equals(uuid))
								.findFirst()
								.orElse(null),
						serversLock
				);
	}

	public Server getServer(String base64UUID)
	{
		return Utils.doLocked
				(
						() -> servers.stream()
								.filter(server -> server.getBase64UUID().equalsIgnoreCase(base64UUID))
								.findFirst()
								.orElse(null),
						serversLock
				);
	}

	public void unregisterServer(Server server)
	{
		Utils.doLocked(() -> servers.remove(server), serversLock);
		uuids.remove(server.getUuid());
	}

	public int getServerCount()
	{
		return severCount.get();
	}

	public UUID newUUID()
	{
		UUID uuid = UUID.randomUUID();
		return (uuids.contains(uuid) || Main.getInstance().getLogManager().checkUsedUUID(uuid)) ? newUUID() : uuid;
	}

	public void updateMessenger(Server srv, MessengerClient client)
	{
		srv.setMessenger(client);
	}

	public void updateServerState(Server srv, ServerStatePacket.ServerState state)
	{
		ProxyServer.getInstance().getPluginManager().callEvent(new ServerChangeStateEvent(srv, state));
		srv.setServerState(state);
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
									data.setPlayer(player);

									Utils.doLocked
											(
													() -> users.add(data),
													usersLock
											);

									return;
								}
							}

							//Else, create data, read in MongoDB then send to Redis

							String base64 = Utils.uuidToBase64(player.getUniqueId());
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

			Utils.doLocked
					(
							() -> users.remove(data),
							usersLock
					);

			UserData.Delay delay = data.getDelayer();
			delay.deadLine = SAVE_DELAY + System.currentTimeMillis();
			disconnectQueue.add(delay);
		}
	}
}
