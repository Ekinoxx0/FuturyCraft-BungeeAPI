package api.log;

import api.Main;
import api.data.Server;
import api.packets.IncPacket;
import api.packets.PacketReceivedEvent;
import api.packets.server.KeepAlivePacket;
import api.packets.server.ServerStatePacket;
import com.mongodb.client.MongoDatabase;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 22/12/2016.
 */
public class KeepAliveManager
{
	private final Map<Server, KeepAlivePacket> cache = new HashMap<>();
	private final ReentrantLock cacheLock = new ReentrantLock();
	private final Condition cacheNotEmpty = cacheLock.newCondition();
	private final Listen listener = new Listen();
	private final MongoDatabase mongoDatabase;
	private final Calendar calendar = Calendar.getInstance();
	private int day;
	private Thread senderThread;
	private Thread watcherThread;
	private boolean init = false;
	private volatile boolean end = false;

	public KeepAliveManager()
	{
		mongoDatabase = Main.getInstance().getMongoClient().getDatabase("keep-alive");
	}

	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialized!");

		ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), listener);

		setupSenderThread();
		setupWatcherThread();
		init = true;
	}

	private void setupSenderThread()
	{
		senderThread = new Thread(() ->
		{
			try
			{
				Thread.sleep(1000 * 30);
			}
			catch (InterruptedException e)
			{
				Main.getInstance().getLogger().log(Level.SEVERE, "Error while pushing keep-alives (Manager: "
						+ this + ")", e);
			}

			while (!end)
			{
				try
				{

					cacheLock.lock();
					try
					{
						if (cache.isEmpty())
						{
							cacheNotEmpty.await();
							continue;
						}

						Main.getInstance().getLogger().info("Sending keep-alives...");

						Document doc = new Document("ts", System.currentTimeMillis());
						cache.keySet().forEach(server ->
								{
									KeepAlivePacket packet = cache.get(server);
									byte[] tps = packet.getLastTPS();
									doc.put(String.valueOf(server.getName()),
											new Document("freeMem", packet.getFreeMemory())
													.append("totMem", packet.getTotalMemory())
													.append("cpu", packet.getProcessCpuLoad())
													.append("tps", Arrays.asList(tps[0], tps[1], tps[2])));
								}
						);

						mongoDatabase.getCollection(
								calendar.get(Calendar.DAY_OF_MONTH) + "-"
										+ calendar.get(Calendar.MONTH) + "-"
										+ calendar.get(Calendar.YEAR)
						).insertOne(doc);


						cache.clear();
					}
					finally
					{
						cacheLock.unlock();
					}

					Thread.sleep(1000 * 60 * 5);

				}
				catch (InterruptedException e)
				{
					if (!end)
						Main.getInstance().getLogger().log(Level.SEVERE, "Error while pushing keep-alives (Manager: "
								+ this + ")", e);
				}
				catch (Exception e)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "Error while pushing keep-alives (Manager: "
							+ this + ")", e);
				}
			}
		}
		);
		senderThread.start();
	}

	private void setupWatcherThread()
	{
		watcherThread = new Thread(() ->
		{
			while (!end)
			{
				try
				{
					Thread.sleep(1000 * 90);

					Main.getInstance().getDataManager().forEachServers(server ->
							{
								if (System.currentTimeMillis() - server.getLastKeepAlive() > 1000 * 90)
								{
									/*ProxyServer.getInstance().getPlayers().stream().filter(player -> player.getServer
											().getInfo().equals(server)).forEach(player -> player.connect(null));*/
									// connect to lobby

									//TODO finish him
									Main.getInstance().getLogger().log(Level.SEVERE, "Server " + server + " did not " +
											"send keep-alive");
								}
							}
					);
				}
				catch (InterruptedException e)
				{
					if (!end)
						Main.getInstance().getLogger().log(Level.SEVERE, "Error while pushing keep-alives (Manager: "
								+ this + ")", e);
				}
				catch (Exception e)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "Error while pushing keep-alives (Manager: "
							+ this + ")", e);
				}
			}
		}
		);
		watcherThread.start();
	}

	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");

		end = true;

		Main.getInstance().getLogger().info(this + " stopped.");
	}

	public class Listen implements Listener
	{
		@EventHandler
		public void onPacket(PacketReceivedEvent event)
		{
			IncPacket packet = event.getPacket();
			if (packet instanceof KeepAlivePacket)
			{

				cacheLock.lock();
				try
				{
					cache.put(event.getFrom(), (KeepAlivePacket) packet);
					cacheNotEmpty.signal();
				}
				finally
				{
					cacheLock.unlock();
				}

				Main.getInstance().getDataManager().updateLastKeepAlive(event.getFrom(), System.currentTimeMillis());
			}
			else if (packet instanceof ServerStatePacket)
			{
				Main.getInstance().getDataManager().updateServerState(event.getFrom(), ((ServerStatePacket) packet)
						.getServerState());
			}
		}
	}

	@Override
	public String toString()
	{
		return "KeepAliveManager{" +
				"cache=" + cache +
				", cacheLock=" + cacheLock +
				", cacheNotEmpty=" + cacheNotEmpty +
				", listener=" + listener +
				", mongoDatabase=" + mongoDatabase +
				", calendar=" + calendar +
				", day=" + day +
				", senderThread=" + senderThread +
				", watcherThread=" + watcherThread +
				", init=" + init +
				", end=" + end +
				'}';
	}
}
