package api;

import api.data.DataManager;
import api.deployer.Deployer;
import api.event.PlayerEvents;
import api.log.KeepAliveManager;
import api.packets.MessengerServer;
import com.mongodb.MongoClient;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;

/**
 * Created by loucass003 on 06/12/16.
 */
public class Main extends Plugin
{
	private static Main instance;

	private final JedisPool jedisPool;
	private final MongoClient mongoClient;

	private final MessengerServer messenger;
	private final Deployer deployer;
	private final DataManager dataManager;
	private final KeepAliveManager keepAliveManager;

	private final PlayerEvents playerEvents;

	public Main()
	{
		instance = this;
		jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
		mongoClient = new MongoClient();
		messenger = new MessengerServer(5555, new String[]{"localhost", "127.0.0.1"});
		deployer = new Deployer();
		dataManager = new DataManager(3 * 60 * 1000); //3min in ms
		playerEvents = new PlayerEvents();
		keepAliveManager = new KeepAliveManager();
	}

	@Override
	public void onEnable()
	{
		File dataFolder = Main.getInstance().getDataFolder();
		if (!dataFolder.exists() && !dataFolder.mkdirs())
			throw new IllegalStateException("Cannot mkdirs data folder");

		deployer.init();
		messenger.init();
		dataManager.init();
		playerEvents.init();
		keepAliveManager.init();

		getLogger().info("FcApiBungee enabled !");
	}

	@Override
	public void onDisable()
	{
		deployer.stop();
		messenger.stop();
		dataManager.stop();
		keepAliveManager.stop();

		if (!jedisPool.isClosed())
			jedisPool.close();
		jedisPool.destroy();
		mongoClient.close();
	}

	public static Main getInstance()
	{
		return instance;
	}

	public JedisPool getJedisPool()
	{
		return jedisPool;
	}

	public MongoClient getMongoClient()
	{
		return mongoClient;
	}

	public MessengerServer getMessenger()
	{
		return messenger;
	}

	public Deployer getDeployer()
	{
		return deployer;
	}

	public DataManager getDataManager()
	{
		return dataManager;
	}

	public PlayerEvents getPlayerEvents()
	{
		return playerEvents;
	}

	public KeepAliveManager getKeepAliveManager()
	{
		return keepAliveManager;
	}
}
