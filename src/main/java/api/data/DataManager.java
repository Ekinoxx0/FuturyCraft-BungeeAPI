package api.data;

import api.Main;
import api.packets.MessengerClient;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by SkyBeast on 19/12/2016.
 */
public class DataManager implements Listener
{
	private final JedisPool jedisPool;
	private final ExecutorService exec = Executors.newCachedThreadPool();
	private final List<UserData> users = new ArrayList<>();
	private final List<Server> servers = new ArrayList<>();

	public DataManager()
	{
		jedisPool = Main.getInstance().getJedisPool();
	}

	public void init()
	{
		ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	public void stop()
	{
		synchronized (servers)
		{
			servers.forEach(srv -> srv.getMessenger().disconnect());
		}
	}

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

				}
		);
	}

	@EventHandler
	public void onQuit(PostLoginEvent event)
	{
		exec.submit(() ->
				{

				}
		);
	}

	public Server findServerByPort(int port)
	{
		Optional<Server> op = servers.stream().filter(srv -> srv.getInfo().getAddress().getPort() == port).findFirst();
		return op.isPresent() ? op.get() : null;
	}

	public UserData getData(ProxiedPlayer player)
	{
		synchronized (users)
		{
			Optional<UserData> op = users.stream().filter(userData -> userData.getPlayer() == player).findFirst();
			return op.isPresent() ? op.get() : null;
		}
	}

	static String integer(int i)
	{
		ByteBuffer buf = ByteBuffer.wrap(new byte[4]);
		buf.putInt(i);
		return new String(buf.array());
	}

	static UUID base64ToUUID(String base64)
	{
		ByteBuffer buf = ByteBuffer.wrap(Base64.getUrlDecoder().decode((base64 + "==").getBytes()));
		long firstLong = buf.getLong();
		long secondLong = buf.getLong();
		return new UUID(firstLong, secondLong);
	}

	static String uuidToBase64(UUID uuid)
	{
		ByteBuffer buf = ByteBuffer.wrap(new byte[16]);
		buf.putLong(uuid.getMostSignificantBits());
		buf.putLong(uuid.getLeastSignificantBits());
		String str = Base64.getUrlEncoder().encodeToString(buf.array());
		return str.substring(0, str.length() - 2);
	}

	public void updateMessenger(Server srv, MessengerClient client)
	{
		srv.setMessenger(client);
	}
}
