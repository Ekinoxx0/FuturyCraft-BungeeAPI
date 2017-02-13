package api.data;

import api.Main;
import api.utils.SimpleManager;
import api.utils.concurrent.ThreadLoop;
import api.utils.concurrent.ThreadLoops;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.function.Consumer;

/**
 * Created by SkyBeast on 12/02/17.
 */
@ToString
public class UserDataManager implements SimpleManager
{
	private static final int SAVE_DELAY = 1000 * 60 * 2;
	private final Map<UUID, UserData> users = new ConcurrentHashMap<>(); //All cached online users -- acquire usersLock before
	// editing
	private final Listen listener = new Listen();
	private final DelayQueue<UserData.Delayer> disconnectQueue = new DelayQueue<>(); //The queue where data is cached
	// before sent to Mongo
	private final ThreadLoop saverThread = setupSaverThread(); //The thread loop used to send all data from the
	// disconnect queue
	private boolean init;
	private volatile boolean end;

	/* ------------ */
	/* - INTERNAL - */
	/* ------------ */

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
	 * Setup the InfiniteThreadLoop for saving users
	 */
	private ThreadLoop setupSaverThread()
	{
		return ThreadLoops.newInfiniteThreadLoop
				(
						() ->
						{
							UserData.Delayer delay = disconnectQueue.take();
							UserData data = delay.parent();

							saveData(data);
						}
				);
	}

	private void saveData(UserData data)
	{
		//todo: save player
	}

	/* ------------- */
	/* - LISTENERS - */
	/* ------------- */

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
			ProxiedPlayer player = event.getPlayer();
			UserData data = reconnectPlayer(player);

			if (data != null) // Already cached!
			{
				addPlayer(data);
				return;
			}

			//todo: get player

			addPlayer(data);
		}

		/**
		 * Find player in the Queue, and remove it from the queue.
		 *
		 * @param player the player to find
		 * @return the player if found
		 */
		private UserData reconnectPlayer(ProxiedPlayer player)
		{
			for (Iterator<UserData.Delayer> ite = disconnectQueue.iterator(); ite.hasNext(); )
			{
				UserData.Delayer delay = ite.next();
				UserData data = delay.parent();
				if (data.getUniqueID().equals(player.getUniqueId())) // Player already cached
				{
					ite.remove();
					return data;
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
			System.out.println("add user -> " + data);
			users.put(data.getUniqueID(), data);
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
			users.remove(data.getUniqueID());
		}

		/**
		 * Add a data to the disconnect queue.
		 *
		 * @param data the data to add
		 */
		private void addToDisconnectQueue(UserData data)
		{

			disconnectQueue.add(data.getDelayer());
		}
	}

	/* --------- */
	/* - UTILS - */
	/* --------- */

	/**
	 * Get a UserData.
	 *
	 * @param id the UUID of the player
	 * @return the player
	 */
	public UserData getData(UUID id)
	{
		return users.get(id);
	}

	/**
	 * Get a UserData.
	 *
	 * @param player the BungeeCord's instance of the player
	 * @return the player
	 */
	public UserData getData(ProxiedPlayer player)
	{
		return getData(player.getUniqueId());
	}

	/**
	 * Do whatever you want with the users, but safely.
	 *
	 * @param consumer what to do
	 */
	public void forEachUsers(Consumer<? super UserData> consumer)
	{
		users.values().forEach(consumer);
	}

	/**
	 * Check is user is in cache
	 *
	 * @param user the player to find
	 * @return the player if found
	 */
	public boolean userCached(UserData user)
	{
		return getData(user.getUniqueID()) != null ||
				disconnectQueue.stream()
						.anyMatch(delay -> delay.parent() == user);
	}
}
