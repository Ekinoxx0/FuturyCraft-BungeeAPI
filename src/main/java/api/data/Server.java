package api.data;

import api.Main;
import api.deployer.DeployerServer;
import api.events.ServerChangeStateEvent;
import api.packets.MessengerClient;
import api.packets.server.ServerStatePacket;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

/**
 * Created by SkyBeast on 19/12/2016.
 */
public class Server
{
	private final DeployerServer deployer;
	private final ServerInfo info;
	private MessengerClient messenger;
	private long lastKeepAlive = -1;
	private ServerStatePacket.ServerState serverState = ServerStatePacket.ServerState.STARTING; //Defaults to STARTING

	Server(DeployerServer deployer, ServerInfo info)
	{
		this.deployer = deployer;
		this.info = info;
	}

	public static Server get(ServerInfo info)
	{
		return Main.getInstance().getDataManager().getServer(info);
	}

	public static Server get(UUID uuid)
	{
		return Main.getInstance().getDataManager().getServer(uuid);
	}

	public static Server get(String base64UUID)
	{
		return Main.getInstance().getDataManager().getServer(base64UUID);
	}

	public long getLastKeepAlive()
	{
		return lastKeepAlive;
	}

	void setLastKeepAlive(long lastKeepAlive)
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

	public ServerStatePacket.ServerState getServerState()
	{
		return serverState;
	}

	void setServerState(ServerStatePacket.ServerState serverState)
	{
		this.serverState = serverState;
		ProxyServer.getInstance().getPluginManager().callEvent(new ServerChangeStateEvent(this, serverState));
	}

	public DeployerServer getDeployer()
	{
		return deployer;
	}

	public ServerInfo getInfo()
	{
		return info;
	}

	public int getOffset()
	{
		return deployer.getOffset();
	}

	public UUID getUUID()
	{
		return deployer.getServerUUID();
	}

	public String getBase64UUID()
	{
		return deployer.getServerBase64UUID();
	}

	public String getName()
	{
		return deployer.getName();
	}

	@Override
	public String toString()
	{
		return "Server{" +
				"messenger=" + messenger +
				", deployer=" + deployer +
				", info=" + info +
				", lastKeepAlive=" + lastKeepAlive +
				", serverState=" + serverState +
				'}';
	}
}
