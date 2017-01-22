package api.events;

import api.data.Server;
import api.packets.IncPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 18/12/2016.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PacketReceivedEvent extends Event
{
	private final Server from;
	private final IncPacket packet;
	private final short transactionID;
}
