package api.panel.packets;

import api.packets.OutPacket;
import api.panel.OutPanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 05/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OutHeaderPanelPacket extends OutPacket implements OutPanelPacket
{
	private final short online;
	private final short maxPlayers;
	private final short servers;

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeShort(online);
		out.writeShort(maxPlayers);
		out.writeShort(servers);
	}
}
