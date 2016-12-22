package api.packets.server;

import api.packets.IncPacket;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 22/12/2016.
 */
public class KeepAlivePacket extends IncPacket
{
	private final long freeMemory;
	private final long totalMemory;
	private final double processCpuLoad;
	private final byte[] lastTPS = new byte[3];

	public KeepAlivePacket(DataInputStream data) throws IOException
	{
		super(data);
		freeMemory = data.readLong();
		totalMemory = data.readLong();
		processCpuLoad = data.readDouble();
		lastTPS[0] = data.readByte();
		lastTPS[1] = data.readByte();
		lastTPS[2] = data.readByte();
	}

	public long getFreeMemory()
	{
		return freeMemory;
	}

	public long getTotalMemory()
	{
		return totalMemory;
	}

	public double getProcessCpuLoad()
	{
		return processCpuLoad;
	}

	public byte[] getLastTPS()
	{
		return lastTPS;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		KeepAlivePacket keepAlivePacket = (KeepAlivePacket) o;

		return freeMemory == keepAlivePacket.freeMemory && totalMemory == keepAlivePacket.totalMemory && Double
				.compare(keepAlivePacket
				.processCpuLoad, processCpuLoad) == 0;

	}

	@Override
	public int hashCode()
	{
		int result;
		long temp;
		result = (int) (freeMemory ^ (freeMemory >>> 32));
		result = 31 * result + (int) (totalMemory ^ (totalMemory >>> 32));
		temp = Double.doubleToLongBits(processCpuLoad);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString()
	{
		return "KeepAlivePacket{" +
				"freeMemory=" + freeMemory +
				", totalMemory=" + totalMemory +
				", processCpuLoad=" + processCpuLoad +
				'}';
	}
}
