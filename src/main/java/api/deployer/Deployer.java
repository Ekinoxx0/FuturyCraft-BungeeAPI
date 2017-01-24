package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import api.config.Template;
import api.config.Variant;
import api.data.Server;
import api.events.ServerDeployedEvent;
import api.utils.SimpleManager;
import api.utils.Utils;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by loucass003 on 14/12/16.
 */
@ToString
public class Deployer implements SimpleManager
{
	private static final int MIN_PORT = 12000;
	private static final int MAX_PORT = 25000;
	private static final int MAX_SERVERS = MAX_PORT - MIN_PORT;
	@Getter
	private DeployerConfig config;
	private boolean init;
	private volatile boolean end;

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialised!");

		config = DeployerConfig.load(new File(Main.getInstance().getDataFolder(), "deployer.json"));
		try
		{
			Utils.deleteFolder(config.getDeployerDir());
			if (!config.getDeployerDir().exists() && !config.getDeployerDir().mkdirs())
			{
				Main.getInstance().getLogger().log(Level.SEVERE, "Unable to mkdirs (Deployer: " +
						this + ')');
				return;
			}
		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while initializing the Deployer (Deployer: " +
					this + ')', e);
		}

		for (Template.LobbyTemplate l : config.getLobbies())
			for (Variant v : l.getVariants())
				for (int i = 0; i < v.getMinServers(); i++)
					addServer(new Lobby(getNextId(), l.getType(), v, getNextPort()));


		for (Template l : config.getGames())
			for (Variant v : l.getVariants())
				for (int i = 0; i < v.getMinServers(); i++)
					addServer(new DeployerServer(getNextId(), DeployerServer.ServerType.GAME, v, getNextPort()));

	}


	public void addServer(DeployerServer deployerServer)
	{
		Server server = Main.getInstance().getDataManager().constructServer(deployerServer, deployerServer.deploy());
		ProxyServer.getInstance().getPluginManager().callEvent(
				new ServerDeployedEvent(server)
		);
		deployerServer.setServer(server);
	}

	public int getNextId()
	{
		return Main.getInstance().getDataManager().getNextDeployerID(MAX_SERVERS);
	}

	public int getNextPort()
	{
		return Main.getInstance().getDataManager().getNextDeployerPort(MIN_PORT, MAX_PORT);
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");

		/*Main.getInstance().getDataManager().forEachServers(server ->
				{
					//Undeploy ?
				}
		);*/

		Main.getInstance().getLogger().info(this + " stopped.");
	}
}
