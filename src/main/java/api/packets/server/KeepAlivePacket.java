package api.packets.server;

import api.packets.IncPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 22/12/2016.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class KeepAlivePacket extends IncPacket
{
	private final short[] lastTPS = new short[3];

	public KeepAlivePacket(DataInputStream data) throws IOException
	{
		super(data);
		lastTPS[0] = data.readShort();
		lastTPS[1] = data.readShort();
		lastTPS[2] = data.readShort();
	}
}
