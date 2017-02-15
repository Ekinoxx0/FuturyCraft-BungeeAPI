package api.log;

import api.Main;
import api.data.Server;
import api.events.PacketReceivedEvent;
import api.packets.IncPacket;
import api.packets.server.KeepAlivePacket;
import api.packets.server.ServerStatePacket;
import api.utils.SimpleManager;
import api.utils.Utils;
import api.utils.concurrent.ThreadLoop;
import api.utils.concurrent.ThreadLoops;
import com.mongodb.client.MongoDatabase;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 22/12/2016.
 */
@ToString
public final class KeepAliveManager implements SimpleManager
{
	private static final int SENDER_LOOP_PERIOD = 1000 * 60 * 5;
	private static final int SENDER_LOOP_INITIAL_DELAY = 1000 * 30;
	private static final int WATCHER_LOOP_PERIOD = 90 * 1000;
	private static final int KILLER_PERIOD = 45 * 1000;
	private final Queue<Server> cache = new ArrayBlockingQueue<>(20);
	private final ReentrantLock cacheLock = new ReentrantLock(); //Thread safe
	private final Condition cacheNotEmpty = cacheLock.newCondition();
	private final Listen listener = new Listen();
	private final MongoDatabase mongoDatabase = Main.getInstance().getMongoClient().getDatabase("keep-alive"); //Will
	// not open connection
	private final DateFormat dateFormat; //dd-MMM-yyyy
	private final DateFormat timeFormat; //HH:mm:ss
	private final ThreadLoop senderThread = setupSenderThread();
	private final ThreadLoop watcherThread = setupWatcherThread();
	private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	private boolean init;
	private volatile boolean end;

	public KeepAliveManager()
	{
		dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT+1"));
	}

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialized!");

		ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), listener);

		senderThread.start();
		watcherThread.start();
		init = true;
	}

	/*
	 * Send all keep-alives to MongoDB
	 */
	private ThreadLoop setupSenderThread()
	{
		return ThreadLoops.newScheduledThreadLoop
				(
						() ->
						{
							cacheLock.lock();
							try
							{
								if (cache.isEmpty())
									cacheNotEmpty.await(); //Await until the cache is not empty
								else
									sendKeepAlives();
							}
							finally
							{
								cacheLock.unlock();
							}
						},
						SENDER_LOOP_INITIAL_DELAY,
						SENDER_LOOP_PERIOD,
						TimeUnit.MILLISECONDS
				);
	}

	/**
	 * Send keep all cached keep alives
	 */
	private void sendKeepAlives()
	{
		Main.getInstance().getLogger().info("Sending keep-alives...");

		Date now = new Date();
		Document doc = new Document("ts", System.currentTimeMillis()).append("time", timeFormat.format(now));
		cache.forEach(server -> putKeepAlive(server, doc));
		mongoDatabase.getCollection(dateFormat.format(now)).insertOne(doc);
		cache.clear();
	}

	/**
	 * Put a keep-alive in a document
	 *
	 * @param server the server
	 * @param doc    the document where to put the keep-alives
	 */
	private void putKeepAlive(Server server, Document doc)
	{
		doc.put(server.getId(),
				new Document("name", server.getName())
						.append("port", server.getPort())
						.append("lastKeepAlive", server.getLastKeepAlive())
						.append("tps", server.getLastTPS())
		);
	}

	/*
	 * Watcher thread which stop servers when no keep-alive were sent
	 */
	private ThreadLoop setupWatcherThread()
	{
		return ThreadLoops.newScheduledThreadLoop
				(
						() -> Main.getInstance().getServerDataManager().forEachServers
								(
										server ->
										{
											if (System.currentTimeMillis() - server.getLastKeepAlive() >
													WATCHER_LOOP_PERIOD)
												fatality(server);
										}
								),
						WATCHER_LOOP_PERIOD,
						TimeUnit.MILLISECONDS
				);
	}

	/**
	 * TODO: Finish him!
	 * (stop the server)
	 */
	private void fatality(Server server)
	{
		//Connect to lobby

		//Stop server

		Main.getInstance().getLogger().log(Level.SEVERE, "Server " + server + " did not send keep-alive");
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");

		senderThread.stop();
		watcherThread.stop();

		end = true;
	}

	public class Listen implements Listener
	{
		private Listen() {}

		/*
		 * Catch keep-alives and save them.
		 *
		 * Update server state.
		 */
		@EventHandler
		public void onPacket(PacketReceivedEvent event)
		{
			IncPacket packet = event.getPacket();
			if (packet instanceof KeepAlivePacket)
			{
				Utils.doLocked
						(
								() ->
								{
									cache.add(event.getFrom());
									cacheNotEmpty.signal(); //Wake up sender thread! You have work to do!
								},
								cacheLock
						);

				event.getFrom().updateData((KeepAlivePacket) packet);
			}
			else if (packet instanceof ServerStatePacket)
			{
				System.out.println("Server State packet ->" + event.getFrom() + " " + packet);
				Main.getInstance().getServerDataManager().updateServerState(event.getFrom(), ((ServerStatePacket)
						packet)
						.getServerState());
			}
		}

		/**
		 * Kill it with fire!
		 */
		private void scheduleKiller(Server server)
		{
			exec.schedule(
					() ->
					{

					},
					KILLER_PERIOD,
					TimeUnit.MILLISECONDS
			);
		}
	}
}
