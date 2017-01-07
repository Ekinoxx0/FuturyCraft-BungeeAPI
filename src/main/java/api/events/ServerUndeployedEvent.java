package api.events;

import api.data.Server;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 06/01/2017.
 */
public class ServerUndeployedEvent extends Event
{
	private final Server server;

	public ServerUndeployedEvent(Server server)
	{
		this.server = server;
	}

	public Server getServer()
	{
		return server;
	}

	@Override
	public String toString()
	{
		return "ServerDeployedEvent{" +
				"server=" + server +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ServerUndeployedEvent that = (ServerUndeployedEvent) o;

		return server != null ? server.equals(that.server) : that.server == null;
	}

	@Override
	public int hashCode()
	{
		return server != null ? server.hashCode() : 0;
	}
}
