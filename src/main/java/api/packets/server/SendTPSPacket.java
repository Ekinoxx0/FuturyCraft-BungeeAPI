package api.packets.server;

import api.packets.IncPacket;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by loucass003 on 07/12/16.
 */
public class SendTPSPacket extends IncPacket
{
	private final byte[] lastTPS;

	public SendTPSPacket(DataInputStream data) throws IOException
	{
		super(data);

		lastTPS = new byte[]{data.readByte(), data.readByte(), data.readByte()};
	}

	public byte[] getLastTPS()
	{
		return lastTPS;
	}

	@Override
	public String toString()
	{
		return "SendTPS{" +
				"lastTPS=" + Arrays.toString(lastTPS) +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SendTPSPacket sendTPS = (SendTPSPacket) o;

		return Arrays.equals(lastTPS, sendTPS.lastTPS);
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(lastTPS);
	}
}
