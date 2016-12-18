package api.packets;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public abstract class IncPacket extends Packet
{
	protected DataInputStream data;

	protected IncPacket(DataInputStream data) throws IOException
	{
		this.data = data;
	}
}
