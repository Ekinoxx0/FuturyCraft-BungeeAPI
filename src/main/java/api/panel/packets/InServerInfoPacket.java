package api.panel.packets;

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
public class InServerInfoPacket extends IncPacket implements IncPanelPacket
{
	private final UUID uuid;
	private final boolean listen;

	public InServerInfoPacket(DataInputStream data) throws IOException
	{
		super(data);
		uuid = new UUID(data.readLong(), data.readLong());
		listen = data.readBoolean();
	}
}
