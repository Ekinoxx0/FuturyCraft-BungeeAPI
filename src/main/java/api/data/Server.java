package api.data;

import api.config.ServerPattern;
import api.deployer.Deployer;
import api.deployer.ServerState;
import api.packets.OutPacket;
import api.packets.ServerMessenger;
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
	private boolean paused;

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
		return ServerDataManager.instance().getServer(info);
	}

	/**
	 * Get a server from its container id.
	 *
	 * @param id the container id
	 * @return the server
	 */
	public static Server get(String id)
	{
		return ServerDataManager.instance().getServer(id);
	}

	/**
	 * Get the name of the server.
	 *
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
		ServerMessenger.instance().sendPacket(id, packet);
	}

	/**
	 * Set the pause state of the server.
	 *
	 * @param pause the pause state
	 */
	public void setPause(boolean pause)
	{
		if(pause) pause();
		else unPause();
	}

	/**
	 * Pause the server.
	 */
	public void pause()
	{
		Deployer.instance().getDockerClient().pauseContainerCmd(id);
		paused = true;
	}

	/**
	 * Un-pause the server.
	 */
	public void unPause()
	{
		Deployer.instance().getDockerClient().unpauseContainerCmd(id);
		paused = false;
	}

}
