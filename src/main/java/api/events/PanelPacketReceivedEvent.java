package api.events;

import api.data.Server;
import api.packets.IncPacket;
import api.panel.PanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by SkyBeast on 18/12/2016.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PanelPacketReceivedEvent extends PacketReceivedEvent
{
	public PanelPacketReceivedEvent(Server from, IncPacket packet, short transactionID)
	{
		super(from, packet, transactionID);
		if (!(packet instanceof PanelPacket))
			throw new IllegalArgumentException("The packet is not a PanelPacket");
	}
}
