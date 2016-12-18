package api.packets;

import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public class PacketReceivedEvent extends Event
{
	private final IncPacket packet;
	private final short transactionID;

	public PacketReceivedEvent(IncPacket packet, short transactionID)
	{
		this.packet = packet;
		this.transactionID = transactionID;
	}

	public IncPacket getPacket()
	{
		return packet;
	}

	public short getTransactionID()
	{
		return transactionID;
	}

	@Override
	public String toString()
	{
		return "PacketReceivedEvent{" +
				"packet=" + packet +
				", transactionID=" + transactionID +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PacketReceivedEvent that = (PacketReceivedEvent) o;

		if (transactionID != that.transactionID) return false;
		return packet != null ? packet.equals(that.packet) : that.packet == null;

	}

	@Override
	public int hashCode()
	{
		int result = packet != null ? packet.hashCode() : 0;
		result = 31 * result + (int) transactionID;
		return result;
	}
}
