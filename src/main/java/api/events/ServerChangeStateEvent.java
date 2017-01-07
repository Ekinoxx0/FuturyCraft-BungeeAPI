package api.events;

import api.data.Server;
import api.packets.server.ServerStatePacket;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 05/01/2017.
 */
public class ServerChangeStateEvent extends Event
{
	private final Server server;
	private final ServerStatePacket.ServerState state;

	public ServerChangeStateEvent(Server server, ServerStatePacket.ServerState state)
	{
		this.server = server;
		this.state = state;
	}

	public Server getServer()
	{
		return server;
	}

	public ServerStatePacket.ServerState getState()
	{
		return state;
	}
}
