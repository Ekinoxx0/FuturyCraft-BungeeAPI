package api.events;

import api.data.Server;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 07/01/2017.
 */
public class NewConsoleLineEvent extends Event
{
	private final Server server;
	private final String line;

	public NewConsoleLineEvent(Server server, String line)
	{
		this.server = server;
		this.line = line;
	}

	public Server getServer()
	{
		return server;
	}

	public String getLine()
	{
		return line;
	}

	@Override
	public String toString()
	{
		return "NewConsoleLineEvent{" +
				"server=" + server +
				", line='" + line + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NewConsoleLineEvent that = (NewConsoleLineEvent) o;

		return server != null ? server.equals(that.server) : that.server == null && (line != null ? line.equals(that
				.line) : that.line == null);

	}

	@Override
	public int hashCode()
	{
		int result = server != null ? server.hashCode() : 0;
		result = 31 * result + (line != null ? line.hashCode() : 0);
		return result;
	}
}
