package api.panel.packets;

import api.packets.OutPacket;
import api.panel.PanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by SkyBeast on 06/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConsoleOutputServerInfoPanelPacket extends OutPacket implements PanelPacket
{
	private final UUID serverUUID;
	private final String line;

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeLong(serverUUID.getMostSignificantBits());
		out.writeLong(serverUUID.getLeastSignificantBits());
		out.writeUTF(line);
	}
}
