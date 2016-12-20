package api.data;

import api.Main;
import api.deployer.DeployerServer;
import api.packets.MessengerClient;
import api.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by SkyBeast on 19/12/2016.
 */
public class DataManager
{
	private final long saveDelay;
	private boolean init = false;
	private volatile boolean end = false;
	private final Listen listener = new Listen();
	private final JedisPool jedisPool;
	private final ExecutorService exec = Executors.newCachedThreadPool();
	private final List<UserData> users = new ArrayList<>();
	private final ReentrantLock usersLock = new ReentrantLock();
	private final List<Server> servers = new ArrayList<>();
	private final ReentrantLock serversLock = new ReentrantLock();
	private final DelayQueue<UserData.Delay> disconnectQueue = new DelayQueue<>();
	private Thread saverThread;

	public DataManager(long saveDelay)
	{
		this.saveDelay = saveDelay;
		jedisPool = Main.getInstance().getJedisPool();
	}

	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialised!");

		ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), listener);
		init = true;
	}

	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");

		serversLock.lock();
		try
		{
			servers.forEach(srv -> srv.getMessenger().disconnect());
		}
		finally
		{
			serversLock.unlock();
		}
		end = true;
	}

	private void setupSaverThread()
	{
		saverThread = new Thread(() ->
		{
			while (!end)
			{
				try
				{
					UserData.Delay delay = disconnectQueue.take();
					UserData user = delay.parent();

					//TODO: MONGODB save

					String prefix = user.getRedisPrefix();
					try (Jedis jedis = jedisPool.getResource())
					{
						jedis.del(prefix + "fc", prefix + "tc", prefix + "rank", prefix + "party", prefix + "friends",
								prefix + "state", prefix + "warn");
					}
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

	private class Listen implements Listener
	{

		@EventHandler
		public void onServerSwitch(ServerConnectEvent event)
		{
			exec.submit(() ->
					{
						try (Jedis jedis = jedisPool.getResource())
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
						Iterator<UserData.Delay> ite = disconnectQueue.iterator();
						for (UserData.Delay delay = ite.next(); ite.hasNext(); )
						{
							UserData data = delay.parent();
							if (data.getPlayer().getUniqueId().equals(event.getPlayer().getUniqueId())) // Player
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

						//Else, read in MongoDB

						//TODO: MONGODB read
					}
			);
		}

		@EventHandler
		public void onQuit(PostLoginEvent event)
		{
			UserData.Delay delay = getData(event.getPlayer()).getDelayer();
			delay.deadLine = saveDelay + System.currentTimeMillis();
			disconnectQueue.add(delay);
		}
	}

	public Server findServerByPort(int port)
	{
		serversLock.lock();
		try
		{
			return servers.stream()
					.filter(srv -> srv.getInfo().getAddress().getPort() == port)
					.findFirst().orElse(null);
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

	public int countServers(Predicate<Server> filter)
	{
		serversLock.lock();
		try
		{
			return (int) servers.stream().filter(filter).count();
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
			List<Integer> ports = servers.stream().map(server -> server.getDeployer().getId()).collect(Collectors
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
					.filter(i -> Utils.isReachable())
					.findFirst().orElse(-1);
		}
		finally
		{
			serversLock.unlock();
		}
	}

	public void constructServer(DeployerServer deployer, ServerInfo info)
	{
		serversLock.lock();
		try
		{
			servers.add(new Server(deployer, info));
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

	public void updateMessenger(Server srv, MessengerClient client)
	{
		srv.setMessenger(client);
	}

	@Override
	public String toString()
	{
		return "DataManager{" +
				"saveDelay=" + saveDelay +
				", init=" + init +
				", end=" + end +
				", listener=" + listener +
				", jedisPool=" + jedisPool +
				", exec=" + exec +
				", users=" + users +
				", usersLock=" + usersLock +
				", servers=" + servers +
				", serversLock=" + serversLock +
				", disconnectQueue=" + disconnectQueue +
				", saverThread=" + saverThread +
				'}';
	}
}
