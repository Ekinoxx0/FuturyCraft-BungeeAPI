package api.panel.packets;

import api.packets.OutPacket;
import api.panel.PanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 05/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OutHeaderPanelPacket extends OutPacket implements PanelPacket
{
	private final short online;
	private final short maxPlayer;
	private final short servers;

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeShort(online);
		out.writeShort(maxPlayer);
		out.writeShort(servers);
	}
}
