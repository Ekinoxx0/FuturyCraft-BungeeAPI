package api.packets.server;

import api.packets.InPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataInput;
import java.io.IOException;

/**
 * Created by SkyBeast on 22/12/2016.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class KeepAlivePacket extends InPacket
{
	private final short[] lastTPS = new short[3];

	public KeepAlivePacket(DataInput data) throws IOException
	{
		super(data);
		lastTPS[0] = data.readShort();
		lastTPS[1] = data.readShort();
		lastTPS[2] = data.readShort();
	}
}
