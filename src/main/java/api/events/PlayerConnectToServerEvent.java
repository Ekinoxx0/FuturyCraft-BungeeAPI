package api.events;

import api.data.Server;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 18/12/2016.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class PlayerConnectToServerEvent extends Event
{
	private final Server from;
	private final UserData user;
	private final ConnectionCause cause;
	private Server to;

	public enum ConnectionCause
	{
		NETWORK_CONNECT,
		SERVER_SWITCH
	}
}
