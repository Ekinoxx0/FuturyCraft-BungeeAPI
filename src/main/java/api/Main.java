package api;

import api.commands.BossBarMessageCommand;
import api.commands.permissons.GroupCommand;
import api.data.ServerDataManager;
import api.data.UserData;
import api.data.UserDataManager;
import api.deployer.Deployer;
import api.lobby.LobbyManager;
import api.log.LogManager;
import api.packets.ServerMessenger;
import api.perms.PermissionsManager;
import api.utils.UtilsListener;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import fr.skybeast.commandcreator.CommandCreator;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
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
@Log
public final class Main extends Plugin
{
	private static Main instance;
	private final MongoClient mongoClient;
	private final MongoDatabase mainDatabase;
	private final Datastore mainDataStore;
	private final Morphia morphia;

	private final ServerMessenger serverMessenger;
	private final ServerDataManager serverDataManager;
	private final UserDataManager userDataManager;
	private final Deployer deployer;
	private final LogManager logManager;
	private final UtilsListener utilsListener;
	private final LobbyManager lobbyManager;
	private final PermissionsManager permissionsManager;

	
	public Main()
	{
		instance = this;
		mongoClient = new MongoClient();
		mainDatabase = mongoClient.getDatabase("FcDeployer");
		morphia = new Morphia();
		morphia.map(UserData.class);
		morphia.getMapper().getOptions().setObjectFactory(new DefaultCreator()
		{
			@Override
			protected ClassLoader getClassLoaderForClass()
			{
				return Main.class.getClassLoader();
			}
		});
		mainDataStore = morphia.createDatastore(mongoClient, "FcDeployer");

		serverMessenger = new ServerMessenger();
		serverDataManager = new ServerDataManager();
		userDataManager = new UserDataManager();
		permissionsManager = new PermissionsManager();
		deployer = new Deployer();
		lobbyManager = new LobbyManager();
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

		serverMessenger.start();
		serverDataManager.start();
		userDataManager.start();
		permissionsManager.start();
		deployer.start();
		logManager.start();
		lobbyManager.start();
		utilsListener.start();

		registerCommand(GroupCommand.class);
		registerCommand(BossBarMessageCommand.class);
		log.info("FcApiBungee enabled!");
	}

	@Override
	public void onDisable()
	{
		serverMessenger.stop();
		serverDataManager.stop();
		userDataManager.stop();
		deployer.stop();
		lobbyManager.stop();
		logManager.stop();
		utilsListener.stop();
		mongoClient.close();
	}

	public static <T extends Event> T callEvent(T event)
	{
		return ProxyServer.getInstance().getPluginManager().callEvent(event);
	}

	public static void registerListener(Listener listener)
	{
		ProxyServer.getInstance().getPluginManager().registerListener(instance, listener);
	}

	public static void registerCommand(Command command)
	{
		ProxyServer.getInstance().getPluginManager().registerCommand(instance, command);
	}

	public static void registerCommand(Class<?> command)
	{
		CommandCreator.registerCommands(command, instance);
	}
}
