package api.log;

import api.Main;
import api.data.Server;
import api.events.PacketReceivedEvent;
import api.packets.IncPacket;
import api.packets.server.KeepAlivePacket;
import api.packets.server.ServerStatePacket;
import api.utils.SimpleManager;
import com.mongodb.client.MongoDatabase;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 22/12/2016.
 */
@ToString
public class KeepAliveManager implements SimpleManager
{
	private final Map<Server, KeepAlivePacket> cache = new HashMap<>();
	private final ReentrantLock cacheLock = new ReentrantLock();
	private final Condition cacheNotEmpty = cacheLock.newCondition();
	private final Listen listener = new Listen();
	private final MongoDatabase mongoDatabase = Main.getInstance().getMongoClient().getDatabase("keep-alive");
	private final DateFormat dateFormat;
	private final DateFormat timeFormat;
	private Thread senderThread;
	private Thread watcherThread;
	private boolean init;
	private volatile boolean end;

	public KeepAliveManager()
	{
		dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
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
						+ this + ')', e);
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

						Date now = new Date();

						Document doc = new Document("ts", System.currentTimeMillis())
								.append("time", timeFormat.format(now));

						cache.keySet().forEach(server ->
								{
									KeepAlivePacket packet = cache.get(server);
									byte[] tps = packet.getLastTPS();
									doc.put(server.getBase64UUID(),
											new Document("freeMem", packet.getFreeMemory())
													.append("name", server.getName())
													.append("offset", server.getOffset())
													.append("totMem", packet.getTotalMemory())
													.append("cpu", packet.getProcessCpuLoad())
													.append("tps", Arrays.asList(tps[0], tps[1], tps[2])));
								}
						);

						mongoDatabase.getCollection(dateFormat.format(now)).insertOne(doc);


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
								+ this + ')', e);
				}
				catch (Exception e)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "Error while pushing keep-alives (Manager: "
							+ this + ')', e);
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
								+ this + ')', e);
				}
				catch (Exception e)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "Error while pushing keep-alives (Manager: "
							+ this + ')', e);
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
		private Listen() {}

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

				event.getFrom().updateData((KeepAlivePacket) packet);
			}
			else if (packet instanceof ServerStatePacket)
			{
				Main.getInstance().getDataManager().updateServerState(event.getFrom(), ((ServerStatePacket) packet)
						.getServerState());
			}
		}
	}
}
