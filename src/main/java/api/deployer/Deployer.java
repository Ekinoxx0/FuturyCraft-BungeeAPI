package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import api.config.Template;
import api.config.Variant;
import api.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by loucass003 on 14/12/16.
 */
public class Deployer
{
	private DeployerConfig config;

	private static final int MIN_PORT = 12000;
	private static final int MAX_PORT = 25000;
	private static final int MAX_SERVERS = MAX_PORT - MIN_PORT;

	public Deployer()
	{
		this.config = new DeployerConfig();
	}

	public void init()
	{
		config = DeployerConfig.load(new File(Main.getInstance().getDataFolder(), "deployer.json"));
		try
		{
			Utils.deleteFolder(config.getDeployerDir());
			if (!config.getDeployerDir().exists() && !config.getDeployerDir().mkdirs())
			{
				Main.getInstance().getLogger().log(Level.SEVERE, "Unable to mkdirs (Deployer: " +
						this + ")");
				return;
			}
		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while initializing the Deployer (Deployer: " +
					this + ")");
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


	public void addServer(DeployerServer server)
	{
		Main.getInstance().getDataManager().constructServer(server, server.deploy());
	}

	public int getNextId()
	{
		return Main.getInstance().getDataManager().getNextDeployerID(MAX_SERVERS);
	}

	public int getNextPort()
	{
		return Main.getInstance().getDataManager().getNextDeployerPort(MIN_PORT, MAX_PORT);
	}

	public DeployerConfig getConfig()
	{
		return config;
	}

	public void stop()
	{
		Main.getInstance().getDataManager().forEachServers(server ->
				{
					//Undeploy ?
				}
		);

		Main.getInstance().getLogger().info(this + " stopped.");
	}

	@Deprecated
	public int countLobby(Lobby.LobbyType t)
	{
		return -1;
	}

	@Override
	public String toString()
	{
		return "Deployer{" +
				"config=" + config +
				'}';
	}
}
