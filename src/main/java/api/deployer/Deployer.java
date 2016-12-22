package api.deployer;

import api.Main;
import api.config.Variant;
import api.data.DataManager;
import api.config.DeployerConfig;
import api.config.Template;
import api.data.Server;
import api.utils.Utils;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.File;
import java.io.IOException;

/**
 * Created by loucass003 on 14/12/16.
 */
public class Deployer
{
	private final DataManager dataManager = Main.getInstance().getDataManager();
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
			if (!config.getDeployerDir().exists())
			{
				if (!config.getDeployerDir().mkdirs())
				{
					Main.getInstance().getLogger().severe("Unable to re-create deployer folder");
					return;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		for(Template.LobbyTemplate l : config.getLobbies())
		{
			for(Variant v : l.getVariants())
			{
				for (int i = 0; i < v.getMinServers(); i++)
				{
					int id = getNextId();
					int port = getNextPort();
					Lobby lobby = new Lobby(id, l.getType(), v, port);

					ServerInfo info = lobby.deploy();

					if (info != null)
					{
						Server srv = dataManager.constructServer(lobby, info);
						lobby.setServer(srv);
					}
					else
					{
						//TODO what if cannot construct server info?
					}
				}
			}
		}

		for(Template l : config.getGames())
		{
			for(Variant v : l.getVariants())
			{
				for (int i = 0; i < v.getMinServers(); i++)
				{
					int id = getNextId();
					int port = getNextPort();
					DeployerServer server = new DeployerServer(id, DeployerServer.ServerType.GAME, v, port);

					ServerInfo info = server.deploy();

					if (info != null)
					{
						Server srv = dataManager.constructServer(server, info);
						server.setServer(srv);
					}
					else
					{
						//TODO what if cannot construct server info?
					}
				}
			}
		}
	}


	public void addServer(DeployerServer server)
	{
		ServerInfo info = server.deploy();
		dataManager.constructServer(server, info);
	}

	public int getNextId()
	{
		return dataManager.getNextDeployerID(MAX_SERVERS);
	}

	public int getNextPort()
	{
		return dataManager.getNextDeployerPort(MIN_PORT, MAX_PORT);
	}


	public DeployerConfig getConfig()
	{
		return config;
	}

	public void stop()
	{
		dataManager.forEachServers(server ->
			{
				//Undeploy ?
			}
		);
	}

	@Deprecated
	public int countLobby(Lobby.LobbyType t)
	{
		return -1;
	}
}
