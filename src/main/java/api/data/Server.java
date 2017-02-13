package api.data;

import api.Main;
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
	private final ServerType type;
	private final Variant variant;
	@Setter(AccessLevel.PACKAGE)
	private MessengerClient messenger;
	@Setter(AccessLevel.PACKAGE)
	private ServerState serverState = ServerState.STARTING;

	private long lastKeepAlive = -1;
	private byte[] lastTPS = new byte[3];

	public Server(String id, ServerType type, Variant variant, ServerInfo info)
	{
		this.id = id;
		this.type = type;
		this.variant = variant;
		this.info = info;
	}

	public static Server get(ServerInfo info)
	{
		return Main.getInstance().getDataManager().getServer(info);
	}

	public String getName()
	{
		return type + "#" + id;
	}

	public void updateData(KeepAlivePacket keepAlivePacket)
	{
		lastKeepAlive = System.currentTimeMillis();
		lastTPS = keepAlivePacket.getLastTPS();
	}

	public int getPort() {
		return info.getAddress().getPort();
	}

	public boolean isLobby()
	{
		return type == ServerType.LOBBY;
	}

	@Getter
	@AllArgsConstructor
	public enum ServerType
	{
		LOBBY("Lobby"),
		GAME("Game");

		private final String name;

		@Override
		public String toString()
		{
			return name;
		}
	}
}
