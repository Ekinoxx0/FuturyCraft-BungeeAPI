package api.events;

import api.data.Server;
import api.data.UserData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 18/12/2016.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlayerConnectToServerEvent extends Event
{
	private final Server server;
	private final UserData user;
	private final ConnectionCause cause;
	private final Server switchTo;

	public enum ConnectionCause
	{
		NETWORK_DISCONNECT,
		SERVER_SWITCH
	}
}
