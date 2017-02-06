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
import com.mongodb.client.model.UpdateOptions;
import lombok.AllArgsConstructor;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used to save players' data to Redis and Mongo.
 * <p>
 * Created by SkyBeast on 19/12/2016.
 */
@ToString
public final class DataManager implements SimpleManager
{
	public static final MongoDatabase DB_UTILS = Main.getInstance().getMongoClient().getDatabase("utils");

	private static final Document EMPTY_DOCUMENT = new Document();
	private static final UpdateOptions UPDATE_OPTIONS_UPSERT = new UpdateOptions().upsert(true);
	private static final long SAVE_DELAY = 1 * 10 * 1000; //The time before the data is put from Redis to Mongo
	private final Listen listener = new Listen();
	private final MongoDatabase usersDB = Main.getInstance().getMongoClient().getDatabase("users"); //Does not open
	// any connection
	private final ExecutorService exec = Executors.newSingleThreadExecutor(); //Used to async some blocking calls
	private final List<UserData> users = new ArrayList<>(); //All cached online users -- acquire usersLock before
	// editing
	private final ReentrantLock usersLock = new ReentrantLock();
	private final List<Server> servers = new ArrayList<>(); //All cached online servers -- acquire serversLock before
	// editing
	private final ReentrantLock serversLock = new ReentrantLock();
	private final DelayQueue<Delay> disconnectQueue = new DelayQueue<>(); //The queue where data is cached before sent
	// to Mongo
	private final List<UUID> uuids = new ArrayList<>(); //All currently used server UUIDs
	private final AtomicInteger serverCount = new AtomicInteger();
	private boolean init;
	private volatile boolean end;
	private final ThreadLoop saverThread = setupSaverThread(); //The thread loop used to send all data from the
	// disconnect queue

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

	/*
	 * Setup the InfiniteThreadLoop for saving
	 */
	private ThreadLoop setupSaverThread()
	{
		return ThreadLoops.newInfiniteThreadLoop
				(
						() ->
						{
							System.out.println("PreTake");
							Delay delay = disconnectQueue.take();
							try (Jedis jedis = Main.getInstance().getJedisPool().getResource())
							{
								System.out.println("PostTake");

								String prefix = delay.redisPrefix;

								//Get from Redis

								Transaction transaction = jedis.multi();
								Response<String> rFC = transaction.get(prefix + ":fc");
								Response<String> rTC = transaction.get(prefix + ":tc");
								Response<String> rState = transaction.get(prefix + ":state");
								Response<String> rGroup = transaction.get(prefix + ":group");
								transaction.exec();

								int fc = Utils.stringToInt(rFC.get());
								int tc = Utils.stringToInt(rTC.get());
								int state = Utils.stringToInt(rState.get());
								String group = rGroup.get();
								//Save to MongoDB

								MongoCollection<Document> col = usersDB.getCollection(delay.base64UUID);
								Document doc = new Document();

								doc.put("fc", fc);
								doc.put("tc", tc);
								doc.put("state", state);
								doc.put("group", group);
								col.replaceOne(EMPTY_DOCUMENT, doc, UPDATE_OPTIONS_UPSERT);

								//Remove from Redis

								jedis.del(prefix + ":fc", prefix + ":tc", prefix + ":rank", prefix + ":party", prefix +
										":friends", prefix + ":state", prefix + ":group", prefix + ":warn");
							}
						}
				);
	}

	/**
	 * Find a server by its port.
	 *
	 * @param port the port of the server
	 * @return the server
	 */
	public Server findServerByPort(int port)
	{
		return Utils.returnLocked
				(
						() -> servers.stream()
								.filter(srv -> srv.getDeployer().getPort() == port)
								.findAny().orElse(null),
						serversLock
				);
	}

	/**
	 * Get a UserData.
	 *
	 * @param player the BungeeCord's representation of the player
	 * @return the player
	 */
	public UserData getData(ProxiedPlayer player)
	{
		return Utils.returnLocked
				(
						() -> users.stream()
								.filter(userData -> userData.getPlayer().equals(player))
								.findFirst().orElse(null),
						serversLock
				);
	}

	/**
	 * Get the online representation of a OfflineUserData.
	 *
	 * @param player the OfflineUserData
	 * @return the online UserData or null if not online
	 */
	public UserData getOnline(OfflineUserData player)
	{
		return Utils.returnLocked
				(
						() -> users.stream()
								.filter(userData -> userData.getUuid().equals(player.getUuid()))
								.findFirst().orElse(null),
						serversLock
				);
	}

	/**
	 * Do whatever you want with the servers, but safely.
	 *
	 * @param consumer what to do
	 */
	public void forEachServers(Consumer<? super Server> consumer)
	{
		Utils.doLocked
				(
						() -> servers.forEach(consumer),
						serversLock
				);
	}

	/**
	 * Do whatever you want with the servers, but safely.
	 *
	 * @param consumer what to do
	 * @param type type filter
	 */
	public void forEachServersByType(Consumer<? super Server> consumer, DeployerServer.ServerType type)
	{
		Utils.doLocked
				(
						() -> servers.stream()
								.filter(server -> server.getDeployer().getType() == type)
								.forEach(consumer),
						serversLock
				);
	}

	/**
	 * Do whatever you want with the users, but safely.
	 *
	 * @param consumer what to do
	 */
	public void forEachUsers(Consumer<? super UserData> consumer)
	{
		Utils.doLocked
				(
						() -> users.forEach(consumer),
						usersLock
				);
	}

	/**
	 * Get the next deployer ID.
	 *
	 * @param maxServers the max bound
	 * @return the next deployer ID
	 */
	public int getNextDeployerID(int maxServers)
	{
		return Utils.returnLocked
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

	/**
	 * Get the next deployer port.
	 *
	 * @param minPort the min bound
	 * @param maxPort the max bound
	 * @return the next deployer port
	 */
	public int getNextDeployerPort(int minPort, int maxPort)
	{
		return Utils.returnLocked
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

	/**
	 * Construct a new Server.
	 *
	 * @param deployer the Deployer of the server
	 * @param info     the ServerInfo of the server
	 * @return the new Server
	 */
	public Server constructServer(DeployerServer deployer, ServerInfo info)
	{
		serverCount.getAndIncrement();

		return Utils.returnLocked
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

	/**
	 * Get a server from the cached online server list.
	 *
	 * @param info the info of the server
	 * @return the server
	 */
	public Server getServer(ServerInfo info)
	{
		if (info == null) return null;

		return Utils.returnLocked
				(
						() -> servers.stream()
								.filter(server -> server.getInfo().equals(info))
								.findFirst()
								.orElse(null),
						serversLock
				);
	}

	/**
	 * Get a server from the cached online server list.
	 *
	 * @param uuid the UUID of the server
	 * @return the server
	 */
	public Server getServer(UUID uuid)
	{
		if (uuid == null) return null;

		return Utils.returnLocked
				(
						() -> servers.stream()
								.filter(server -> server.getUuid().equals(uuid))
								.findFirst()
								.orElse(null),
						serversLock
				);
	}

	/**
	 * Get a server from the cached online server list.
	 *
	 * @param base64UUID the Base64 representation of the UUID of the server
	 * @return the server
	 */
	public Server getServer(String base64UUID)
	{
		if (base64UUID == null) return null;

		return Utils.returnLocked
				(
						() -> servers.stream()
								.filter(server -> server.getBase64UUID().equalsIgnoreCase(base64UUID))
								.findFirst()
								.orElse(null),
						serversLock
				);
	}

	/**
	 * Unregister a server from the cached online server list.
	 *
	 * @param server the server to remove
	 */
	public void unregisterServer(Server server)
	{
		serverCount.getAndIncrement();
		Utils.doLocked(() -> servers.remove(server), serversLock);
		uuids.remove(server.getUuid());
	}

	/**
	 * Get the server count.
	 *
	 * @return the server count
	 */
	public int getServerCount()
	{
		return serverCount.get();
	}

	/**
	 * Get a fresh new UUID, which is safe to use for a server.
	 *
	 * @return the new UUID
	 */
	public UUID newUUID()
	{
		UUID uuid = UUID.randomUUID();
		return (uuids.contains(uuid) || Main.getInstance().getLogManager().checkUsedUUID(uuid)) ? newUUID() : uuid;
	}

	/**
	 * Update a server's MessengerClient.
	 *
	 * @param srv    the server to update
	 * @param client the new client
	 */
	public void updateMessenger(Server srv, MessengerClient client)
	{
		srv.setMessenger(client);
	}

	/**
	 * Update a server's state.
	 *
	 * @param srv   the server to update
	 * @param state the new state
	 */
	public void updateServerState(Server srv, ServerStatePacket.ServerState state)
	{
		ProxyServer.getInstance().getPluginManager().callEvent(new ServerChangeStateEvent(srv, state));
		srv.setServerState(state);
	}

	public class Listen implements Listener
	{
		private Listen() {}

		/*
		 * Used to add the data to the cached online player list.
		 *
		 * If the player is cached in the disconnectQueue, then the player is pulled from Redis.
		 * Else, the player is pulled from Mongo.
		 */
		@EventHandler(priority = EventPriority.HIGHEST)
		public void onJoin(ServerConnectEvent event)
		{
			exec.submit(() ->
					{
						try (Jedis jedis = Main.getInstance().getJedisPool().getResource())
						{
							ProxiedPlayer player = event.getPlayer();

							//Get data if cached

							UserData data = findPlayer(player);
							if (data != null)
							{
								addPlayer(data); //Add the player to connected players list
								return; //No need to find data in Mongo, so stop there
							}

							//Else, create data,
							String base64 = Utils.uuidToBase64(player.getUniqueId());
							String redisPrefix = "u:" + base64;
							data = new UserData(player, base64, redisPrefix);

							//Get from Mongo
							MongoCollection<Document> col = usersDB.getCollection(base64);
							Document doc = col.find().first();
							if (doc == null) //The user is a NEWBIE
							{
								doc = new Document();
								doc.put("firstJoin", System.currentTimeMillis()); //Keep track of the first connection
							}

							//Get values or default
							int fc = doc.getInteger("fc", 0);
							int tc = doc.getInteger("tc", 0);
							int rank = doc.getInteger("rank", 0);
							int state = doc.getInteger("state", 0);
							String group = doc.getString("group");

							//Then send to Redis
							sendToRedis(jedis, redisPrefix, fc, tc, rank, state, group);

							addPlayer(data); //Finally, add the player to connected players list
						}
					}
			);
		}

		/**
		 * Save data to Redis.
		 *
		 * @param jedis       the Jedis instance
		 * @param redisPrefix the Redis prefix of the player
		 * @param fc          the FuturyCoins
		 * @param tc          the TurfuryCoins
		 * @param rank        the rank
		 * @param state       the player's state
		 */
		private void sendToRedis(Jedis jedis, String redisPrefix, int fc, int tc, int rank, int state, String group)
		{
			Transaction transaction = jedis.multi();
			transaction.set(redisPrefix + ":fc", Utils.intToString(fc));
			transaction.set(redisPrefix + ":tc", Utils.intToString(tc));
			transaction.set(redisPrefix + ":rank", Utils.intToString(rank));
			transaction.set(redisPrefix + ":state", Utils.intToString(state));
			transaction.set(redisPrefix + ":group", group);
			transaction.exec();
		}

		/**
		 * Find player in the Queue, and remove it from the queue.
		 *
		 * @param player the player to find
		 * @return the player if found
		 */
		private UserData findPlayer(ProxiedPlayer player)
		{
			for (Iterator<Delay> ite = disconnectQueue.iterator(); ite.hasNext(); )
			{
				Delay delay = ite.next();
				if (delay.uuid.equals(player.getUniqueId())) // Player already cached in Redis
				{
					ite.remove();

					return new UserData(player, delay.base64UUID, delay.redisPrefix);
				}
			}
			return null;
		}

		/**
		 * Add a player to the cached online player list.
		 *
		 * @param data the player
		 */
		private void addPlayer(UserData data)
		{
			Utils.doLocked
					(
							() -> users.add(data),
							usersLock
					);
		}

		/*
		 * Remove the data from the cached online player list, then add it to the disconnectQueue
		 */
		@EventHandler
		public void onQuit(PlayerDisconnectEvent event)
		{
			UserData data = getData(event.getPlayer());

			removeData(data);

			addToDisconnectQueue(data);
		}

		/**
		 * Remove data from the cached online player list.
		 *
		 * @param data the data to remove
		 */
		private void removeData(UserData data)
		{
			Utils.doLocked
					(
							() -> users.remove(data),
							usersLock
					);
		}

		/**
		 * Add a data to the disconnect queue.
		 *
		 * @param data the data to add
		 */
		private void addToDisconnectQueue(UserData data)
		{
			disconnectQueue.add(new Delay
					(
							SAVE_DELAY + System.currentTimeMillis(),
							data.getBase64UUID(),
							data.getRedisPrefix(),
							data.getUuid()
					));
		}
	}

	/**
	 * The class used in the DelayedQueue.
	 */
	@AllArgsConstructor
	private static class Delay implements Delayed
	{
		final long deadLine;
		final String base64UUID;
		final String redisPrefix;
		final UUID uuid;

		@Override
		public String toString()
		{
			return "Delay{" +
					"deadLine=" + deadLine +
					", getDelay(TimeUnit.MILLISECONDS)=" + getDelay(TimeUnit.MILLISECONDS) +
					", base64UUID='" + base64UUID + '\'' +
					", redisPrefix='" + redisPrefix + '\'' +
					", uuid=" + uuid +
					'}';
		}

		@Override
		public long getDelay(TimeUnit unit) //Negative when deadLine passed
		{
			return unit.convert(deadLine - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed o) //Older first
		{
			return (deadLine == ((Delay) o).deadLine ? 0 : (deadLine > ((Delay) o).deadLine ? -1 : 1));
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof Delay && ((Delay) o).deadLine == deadLine;
		}

		@Override
		public int hashCode()
		{
			return (int) (deadLine ^ (deadLine >>> 32));
		}
	}

}
