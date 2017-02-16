package api.packets;

import java.io.DataInput;
import java.io.IOException;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public abstract class InPacket extends Packet
{

	protected InPacket(DataInput data) throws IOException {}
}
