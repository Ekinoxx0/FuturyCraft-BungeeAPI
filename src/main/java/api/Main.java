package api;

import api.commands.BossBarMessageCommand;
import api.commands.DispatchCommand;
import api.data.ServerDataManager;
import api.data.UserData;
import api.data.UserDataManager;
import api.deployer.Deployer;
import api.lobby.LobbyManager;
import api.log.KeepAliveManager;
import api.log.LogManager;
import api.packets.MessengerServer;
import api.panel.PanelManager;
import api.perms.PermissionsManager;
import api.utils.UtilsListener;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.plugin.Plugin;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.DefaultCreator;

import java.io.File;

/**
 * Created by loucass003 on 06/12/16.
 */
@ToString
@Getter
public final class Main extends Plugin
{
	private static Main instance;
	private final MongoClient mongoClient;
	private final MongoDatabase mainDatabase;
	private final Datastore mainDataStore;
	private final Morphia morphia;

	private final MessengerServer messenger;
	private final ServerDataManager serverDataManager;
	private final UserDataManager userDataManager;
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
		mongoClient = new MongoClient();
		mainDatabase = mongoClient.getDatabase("FcDeployer");
		morphia = new Morphia();
		morphia.map(UserData.class);
		morphia.getMapper().getOptions().setObjectFactory(new DefaultCreator() {
			@Override
			protected ClassLoader getClassLoaderForClass()
			{
				return Main.class.getClassLoader();
			}
		});
		mainDataStore = morphia.createDatastore(mongoClient, "FcDeployer");

		messenger = new MessengerServer();
		serverDataManager = new ServerDataManager();
		userDataManager = new UserDataManager();
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
		serverDataManager.init();
		userDataManager.init();
		permsManager.init();
		deployer.init();
		logManager.init();
		lobbyManager.init();
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
		serverDataManager.stop();
		userDataManager.stop();
		deployer.stop();
		lobbyManager.stop();
		keepAliveManager.stop();
		panelManager.stop();
		logManager.stop();
		utilsListener.stop();
		mongoClient.close();
	}
}
