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
public class KeepAlivePacket extends IncPacket
{
	private final long freeMemory;
	private final long totalMemory;
	private final float processCpuLoad;
	private final byte[] lastTPS = new byte[3];

	public KeepAlivePacket(DataInputStream data) throws IOException
	{
		super(data);
		freeMemory = data.readLong();
		totalMemory = data.readLong();
		processCpuLoad = data.readFloat();
		data.readFully(lastTPS);
	}
}
