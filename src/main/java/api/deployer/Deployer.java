package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import api.config.ServerConfig;
import api.config.ServerTemplate;
import api.data.DataManager;
import api.utils.Utils;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by loucass003 on 14/12/16.
 */
public class Deployer implements Listener
{
	private final DataManager dataManager = Main.getInstance().getDataManager();
	public DeployerConfig config;

	private static final int MIN_PORT = 12000;
	private static final int MAX_PORT = 25000;
	private static final int MAX_SERVERS = MAX_PORT - MIN_PORT;

	public Deployer()
	{
		this.config = new DeployerConfig();
	}

	public void init()
	{
		config.load(new File(Main.getInstance().getDataFolder(), "deployer.yml"));
		try
		{
			Utils.deleteFolder(DeployerConfig.getDeployerDir());
			if (!DeployerConfig.getDeployerDir().exists())
			{
				if (!DeployerConfig.getDeployerDir().mkdirs())
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

		for (Map.Entry<String, Map<String, Object>> entry : config.getServers().entrySet())
		{
			ServerConfig srvConf = new ServerConfig(entry.getKey(), entry.getValue());
			for (ServerTemplate template : srvConf.getTemplates())
			{
				for (int i = 0; i < template.getMinServers(); i++)
				{
					DeployerServer server;
					int id = getNextId();
					int port = getNextPort();
					if (srvConf.getType() == DeployerServer.ServerType.LOBBY)
						server = new Lobby(id, srvConf.getName(), template, port);
					else
						server = new DeployerServer(id, srvConf.getName(), srvConf.getType(), template, port);

					ServerInfo info = server.deploy();

					dataManager.constructServer(server, info);
				}
			}
		}
	}

	public int getNextId()
	{
		return dataManager.getNextDeployerID(MAX_SERVERS);
	}

	public int getNextPort()
	{
		return dataManager.getNextDeployerPort(MIN_PORT, MAX_PORT);
	}

	public void stop()
	{
		dataManager.forEachServers(server ->
				{
					//Undeploy ?
				}
		);
	}

	public int countLobby(Lobby.LobbyType t)
	{
		return dataManager.countServers(server ->
				server.getDeployer().getType() == DeployerServer.ServerType.LOBBY
						&& ((Lobby) server.getDeployer()).getLobbyType() == t);
	}
}
