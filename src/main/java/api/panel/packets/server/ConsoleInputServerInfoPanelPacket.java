package api.panel.packets.server;

import api.packets.IncPacket;
import api.panel.IncPanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by SkyBeast on 06/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConsoleInputServerInfoPanelPacket extends IncPacket implements IncPanelPacket
{
	private final UUID serverUUID;
	private final String in;

	public ConsoleInputServerInfoPanelPacket(DataInputStream data) throws IOException
	{
		super(data);
		serverUUID = new UUID(data.readLong(), data.readLong());
		in = data.readUTF();
	}
}
