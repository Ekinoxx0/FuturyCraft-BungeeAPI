package api.packets;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public abstract class OutPacket extends Packet
{
	public OutPacket() {}

	public void write(DataOutputStream out) throws IOException {}
}
