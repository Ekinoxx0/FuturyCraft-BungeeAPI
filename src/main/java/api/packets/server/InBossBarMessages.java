package api.packets.server;

import api.packets.IncPacket;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by loucass003 on 2/4/17.
 */
public class InBossBarMessages extends IncPacket
{
	public InBossBarMessages(DataInputStream data) throws IOException
	{
		super(data);
	}
}
