package api.events;

import api.data.Server;
import api.packets.server.ServerStatePacket;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 05/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ServerChangeStateEvent extends Event
{
	private final Server server;
	private final ServerStatePacket.ServerState state;
}
