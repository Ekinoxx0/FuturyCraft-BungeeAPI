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

	Server(DeployerServer deployer, ServerInfo info)
	{
		this.deployer = deployer;
		this.info = info;
	}

	public static Server get(ServerInfo info)
	{
		return Main.getInstance().getDataManager().getServer(info);
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

	@Override
	public String toString()
	{
		return "Server{" +
				"messenger=" + messenger +
				", template=" + deployer +
				", info=" + info +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Server server = (Server) o;

		return messenger != null ? messenger.equals(server.messenger) : server.messenger == null && (deployer != null
				? deployer.equals(server.deployer) : server.deployer == null && (info != null ? info.equals(server
				.info) : server.info == null));

	}

	@Override
	public int hashCode()
	{
		int result = messenger != null ? messenger.hashCode() : 0;
		result = 31 * result + (deployer != null ? deployer.hashCode() : 0);
		result = 31 * result + (info != null ? info.hashCode() : 0);
		return result;
	}
}
