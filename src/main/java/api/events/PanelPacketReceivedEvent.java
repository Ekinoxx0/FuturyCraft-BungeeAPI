package api.events;

import api.data.Server;
import api.packets.IncPacket;
import api.panel.IncPanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by SkyBeast on 18/12/2016.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PanelPacketReceivedEvent extends PacketReceivedEvent
{
	public PanelPacketReceivedEvent(Server from, IncPanelPacket packet, short transactionID)
	{
		super(from, (IncPacket) packet, transactionID);
	}
}
