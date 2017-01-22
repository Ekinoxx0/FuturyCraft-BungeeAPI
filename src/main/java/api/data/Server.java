package api.data;

import api.Main;
import api.deployer.DeployerServer;
import api.packets.MessengerClient;
import api.packets.server.ServerStatePacket;
import lombok.*;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

/**
 * Created by SkyBeast on 19/12/2016.
 */
@Data
public class Server
{
	private final DeployerServer deployer;
	private final ServerInfo info;
	@Setter(AccessLevel.PACKAGE)
	private MessengerClient messenger;
	@Setter(AccessLevel.PACKAGE)
	private long lastKeepAlive = -1;
	@Setter(AccessLevel.PACKAGE)
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

	public int getOffset()
	{
		return deployer.getOffset();
	}

	public UUID getUUID()
	{
		return deployer.getUuid();
	}

	public String getBase64UUID()
	{
		return deployer.getBase64UUID();
	}

	public String getName()
	{
		return deployer.getName();
	}
}
