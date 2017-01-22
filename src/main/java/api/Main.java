package api;

import api.commands.DispatchCommand;
import api.data.DataManager;
import api.deployer.Deployer;
import api.event.PlayerEvents;
import api.log.KeepAliveManager;
import api.log.LogManager;
import api.packets.MessengerServer;
import api.panel.PanelManager;
import com.mongodb.MongoClient;
import lombok.ToString;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;

/**
 * Created by loucass003 on 06/12/16.
 */
@ToString
public class Main extends Plugin
{
	private static Main instance;

	private final JedisPool jedisPool;
	private final MongoClient mongoClient;

	private final MessengerServer messenger;
	private final DataManager dataManager;
	private final Deployer deployer;
	private final PlayerEvents playerEvents;
	private final KeepAliveManager keepAliveManager;
	private final PanelManager panelManager;
	private final LogManager logManager;

	public Main()
	{
		instance = this;

		jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
		mongoClient = new MongoClient();

		messenger = new MessengerServer(5555, "localhost", "127.0.0.1");
		dataManager = new DataManager(3 * 60 * 1000); //3min in ms
		deployer = new Deployer();
		playerEvents = new PlayerEvents();
		keepAliveManager = new KeepAliveManager();
		panelManager = new PanelManager();
		logManager = new LogManager();
	}

	public static Main getInstance()
	{
		return instance;
	}

	@Override
	public void onEnable()
	{
		File dataFolder = getInstance().getDataFolder();
		if (!dataFolder.exists() && !dataFolder.mkdirs())
			throw new IllegalStateException("Cannot mkdirs data folder");

		messenger.init();
		dataManager.init();
		deployer.init();
		playerEvents.init();
		keepAliveManager.init();
		panelManager.init();
		logManager.init();

		getProxy().getPluginManager().registerCommand(this, new DispatchCommand());

		getLogger().info("FcApiBungee enabled!");
	}

	@Override
	public void onDisable()
	{
		messenger.stop();
		dataManager.stop();
		deployer.stop();
		playerEvents.stop();
		keepAliveManager.stop();
		panelManager.stop();
		logManager.stop();

		jedisPool.close();
		jedisPool.destroy();
		mongoClient.close();
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

	public DataManager getDataManager()
	{
		return dataManager;
	}

	public Deployer getDeployer()
	{
		return deployer;
	}

	public PlayerEvents getPlayerEvents()
	{
		return playerEvents;
	}

	public KeepAliveManager getKeepAliveManager()
	{
		return keepAliveManager;
	}

	public PanelManager getPanelManager()
	{
		return panelManager;
	}

	public LogManager getLogManager()
	{
		return logManager;
	}
}
