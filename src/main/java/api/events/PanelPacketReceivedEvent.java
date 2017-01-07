package api.events;

import api.data.Server;
import api.packets.IncPacket;
import api.panel.PanelPacket;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public class PanelPacketReceivedEvent extends PacketReceivedEvent
{
	public PanelPacketReceivedEvent(Server from, IncPacket packet, short transactionID)
	{
		super(from, packet, transactionID);
		assert packet instanceof PanelPacket;
	}
}
