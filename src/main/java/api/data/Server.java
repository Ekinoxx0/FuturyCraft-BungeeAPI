package api.data;

import api.Main;
import api.deployer.DeployerServer;
import api.packets.MessengerClient;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Created by SkyBeast on 19/12/2016.
 */
public class Server
{
	private MessengerClient messenger;
	private final DeployerServer deployer;
	private final ServerInfo info;

	private long lastKeepAlive;

	Server(DeployerServer deployer, ServerInfo info)
	{
		this.deployer = deployer;
		this.info = info;
	}

	public static Server get(ServerInfo info)
	{
		return Main.getInstance().getDataManager().getServer(info);
	}

	public long getLastKeepAlive()
	{
		return lastKeepAlive;
	}

	public void setLastKeepAlive(long lastKeepAlive)
	{
		this.lastKeepAlive = lastKeepAlive;
	}

	public MessengerClient getMessenger()
	{
		return messenger;
	}

	void setMessenger(MessengerClient messenger)
	{
		this.messenger = messenger;
	}

	public DeployerServer getDeployer()
	{
		return deployer;
	}

	public ServerInfo getInfo()
	{
		return info;
	}

	public int getID()
	{
		return deployer.getId();
	}

	@Override
	public String toString()
	{
		return "Server{" +
				"messenger=" + messenger +
				", deployer=" + deployer +
				", info=" + info +
				", lastKeepAlive=" + lastKeepAlive +
				'}';
	}
}
