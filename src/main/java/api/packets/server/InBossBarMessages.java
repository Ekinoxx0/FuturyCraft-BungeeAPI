package api.packets.server;

import api.packets.InPacket;

import java.io.DataInput;
import java.io.IOException;

/**
 * Created by loucass003 on 2/4/17.
 */
public class InBossBarMessages extends InPacket
{
	public InBossBarMessages(DataInput data) throws IOException
	{
		super(data);
	}
}
