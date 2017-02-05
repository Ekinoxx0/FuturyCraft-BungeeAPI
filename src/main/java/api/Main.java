package api;

import api.commands.BossBarMessageCommand;
import api.commands.DispatchCommand;
import api.data.DataManager;
import api.deployer.Deployer;
import api.lobby.LobbyManager;
import api.log.KeepAliveManager;
import api.log.LogManager;
import api.packets.MessengerServer;
import api.panel.PanelManager;
import api.perms.PermissionsManager;
import api.utils.UtilsListener;
import com.mongodb.MongoClient;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;

/**
 * Created by loucass003 on 06/12/16.
 */
@ToString
@Getter
public final class Main extends Plugin
{
	private static Main instance;

	private final JedisPool jedisPool;
	private final MongoClient mongoClient;

	private final MessengerServer messenger;
	private final DataManager dataManager;
	private final Deployer deployer;
	private final KeepAliveManager keepAliveManager;
	private final PanelManager panelManager;
	private final LogManager logManager;
	private final UtilsListener utilsListener;
	private final LobbyManager lobbyManager;
	private final PermissionsManager permsManager;

	public Main()
	{
		instance = this;

		jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
		mongoClient = new MongoClient();
		messenger = new MessengerServer();
		dataManager = new DataManager();
		permsManager = new PermissionsManager();
		deployer = new Deployer();
		lobbyManager = new LobbyManager();
		keepAliveManager = new KeepAliveManager();
		panelManager = new PanelManager();
		logManager = new LogManager();
		utilsListener = new UtilsListener();

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
		permsManager.init();
		deployer.init();
		logManager.init();
		lobbyManager.init();
		deployer.initServers();
		keepAliveManager.init();
		panelManager.init();
		utilsListener.init();

		getProxy().getPluginManager().registerCommand(this, new DispatchCommand());
		getProxy().getPluginManager().registerCommand(this, new BossBarMessageCommand());
		getLogger().info("FcApiBungee enabled!");
	}

	@Override
	public void onDisable()
	{
		messenger.stop();
		dataManager.stop();
		deployer.stop();
		lobbyManager.stop();
		keepAliveManager.stop();
		panelManager.stop();
		logManager.stop();
		utilsListener.stop();

		jedisPool.close();
		jedisPool.destroy();
		mongoClient.close();
	}
}
