package api.events;

import api.data.Server;
import api.data.UserData;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public class PlayerDisconnectFromServerEvent extends Event
{
	private final Server server;
	private final UserData user;
	private final ConnectionCause cause;
	private final Server switchTo;

	public PlayerDisconnectFromServerEvent(Server server, UserData user, ConnectionCause cause, Server switchTo)
	{
		this.server = server;
		this.user = user;
		this.cause = cause;
		this.switchTo = switchTo;
	}

	public Server getServer()
	{
		return server;
	}

	public UserData getUser()
	{
		return user;
	}

	public ConnectionCause getCause()
	{
		return cause;
	}

	public Server getSwitchTo()
	{
		return switchTo;
	}

	@Override
	public String toString()
	{
		return "PlayerDisconnectFromServerEvent{" +
				"server=" + server +
				", user=" + user +
				", cause=" + cause +
				", switchTo=" + switchTo +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PlayerDisconnectFromServerEvent that = (PlayerDisconnectFromServerEvent) o;

		return server != null ? server.equals(that.server) : that.server == null && (user != null ? user.equals(that
				.user) : that.user == null && cause == that.cause && (switchTo != null ? switchTo.equals(that
				.switchTo) : that.switchTo == null));
	}

	@Override
	public int hashCode()
	{
		int result = server != null ? server.hashCode() : 0;
		result = 31 * result + (user != null ? user.hashCode() : 0);
		result = 31 * result + (cause != null ? cause.hashCode() : 0);
		result = 31 * result + (switchTo != null ? switchTo.hashCode() : 0);
		return result;
	}

	public enum ConnectionCause
	{
		NETWORK_DISCONNECT,
		SERVER_SWITCH
	}
}
