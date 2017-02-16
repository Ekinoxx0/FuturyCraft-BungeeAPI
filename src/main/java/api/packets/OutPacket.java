package api.packets;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public abstract class OutPacket extends Packet
{
	public void write(DataOutput out) throws IOException {}
}
