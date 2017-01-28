package api.data;

import api.Main;
import api.deployer.DeployerServer;
import api.deployer.Lobby;
import api.packets.MessengerClient;
import api.packets.server.KeepAlivePacket;
import api.packets.server.ServerStatePacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

/**
 * Created by SkyBeast on 19/12/2016.
 */
@Getter
public class Server
{
	private final DeployerServer deployer;
	private final ServerInfo info;
	@Setter(AccessLevel.PACKAGE)
	private MessengerClient messenger;
	@Setter(AccessLevel.PACKAGE)
	private ServerStatePacket.ServerState serverState = ServerStatePacket.ServerState.STARTING; //Defaults to STARTING

	private long lastKeepAlive = -1;
	private final long minMemory;
	private long freeMemory;
	private long totalMemory;
	private final long maxMemory;
	private float processCpuLoad;
	private byte[] lastTPS = new byte[3];

	public Server(DeployerServer deployer, ServerInfo info)
	{
		this.deployer = deployer;
		this.info = info;
		minMemory = deployer.getVariant().getMaxRam() << 20;
		maxMemory = deployer.getVariant().getMinRam() << 20;
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

	public UUID getUuid()
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

	public void updateData(KeepAlivePacket keepAlivePacket)
	{
		lastKeepAlive = System.currentTimeMillis();
		freeMemory = keepAlivePacket.getFreeMemory();
		totalMemory = keepAlivePacket.getTotalMemory();
		processCpuLoad = keepAlivePacket.getProcessCpuLoad();
		lastTPS = keepAlivePacket.getLastTPS();
	}

	public boolean isLobby()
	{
		return deployer instanceof Lobby;
	}

	public boolean isStarted()
	{
		return deployer.isStarted();
	}
}
