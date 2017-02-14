package api.data;

import api.Main;
import api.config.ServerPattern;
import api.config.Variant;
import api.deployer.ServerState;
import api.packets.MessengerClient;
import api.packets.server.KeepAlivePacket;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Created by SkyBeast on 19/12/2016.
 */
@Getter
public class Server
{
	private final String id;
	private final ServerInfo info;
	private final ServerPattern pattern;
	@Setter(AccessLevel.PACKAGE)
	private MessengerClient messenger;
	@Setter(AccessLevel.PACKAGE)
	private ServerState serverState = ServerState.STARTING;

	private long lastKeepAlive = -1;
	private short[] lastTPS = new short[3];

	public Server(String id, ServerPattern pattern, ServerInfo info)
	{
		this.id = id;
		this.pattern = pattern;
		this.info = info;
	}

	public static Server get(ServerInfo info)
	{
		return Main.getInstance().getDataManager().getServer(info);
	}

	public void updateData(KeepAlivePacket keepAlivePacket)
	{
		lastKeepAlive = System.currentTimeMillis();
		lastTPS = keepAlivePacket.getLastTPS();
	}

	public String getName()
	{
		return String.format("%s:%s#%s", pattern.getName(), pattern.getVariant().getName(), id);
	}

	public boolean isLobby()
	{
		return pattern.has("type", "lobby");
	}

	public int getPort() {
		return info.getAddress().getPort();
	}

}
