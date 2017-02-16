package api.data;

import api.Main;
import api.config.ServerPattern;
import api.deployer.ServerState;
import api.packets.OutPacket;
import api.packets.server.KeepAlivePacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.File;

/**
 * Created by SkyBeast on 19/12/2016.
 */
@Getter
public class Server
{
	private final String id;
	private final ServerInfo info;
	private final ServerPattern pattern;
	private final File tempFolder;
	@Setter(AccessLevel.PACKAGE)
	private ServerState serverState = ServerState.STARTING;

	private long lastKeepAlive = -1;
	private short[] lastTPS = new short[3];

	public Server(String id, ServerPattern pattern, File tempFolder, ServerInfo info)
	{
		this.id = id;
		this.pattern = pattern;
		this.tempFolder = tempFolder;
		this.info = info;
	}

	/**
	 * Get a server from the cached online server list.
	 *
	 * @param info the info of the server
	 * @return the server
	 */
	public static Server get(ServerInfo info)
	{
		return Main.getInstance().getServerDataManager().getServer(info);
	}

	/**
	 * Get a server from its container id.
	 *
	 * @param id the container id
	 * @return the server
	 */
	public static Server get(String id)
	{
		return Main.getInstance().getServerDataManager().getServer(id);
	}

	public void updateData(KeepAlivePacket keepAlivePacket)
	{
		lastKeepAlive = System.currentTimeMillis();
		lastTPS = keepAlivePacket.getLastTPS();
	}

	/**
	 * Get the name of the server.
	 * @return a human readable name for the server
	 */
	public String getName()
	{
		return String.format("%s:%s#%s", pattern.getName(), pattern.getVariant().getName(), id);
	}

	/**
	 * Check if the server has "lobby" type set.
	 *
	 * @return whether or not the server is a lobby
	 */
	public boolean isLobby()
	{
		return pattern.has("type", "lobby");
	}

	/**
	 * Get the exposed port of the server.
	 *
	 * @return the exposed port
	 */
	public int getPort()
	{
		return info.getAddress().getPort();
	}

	/**
	 * Send a packet to the server.
	 *
	 * @param packet the packet to send
	 */
	public void sendPacket(OutPacket packet)
	{
		Main.getInstance().getServerMessenger().sendPacket(id, packet);
	}

}
