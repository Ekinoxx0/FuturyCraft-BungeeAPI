package api.events;

import api.data.Server;
import api.packets.IncPacket;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public class PacketReceivedEvent extends Event
{
	private final Server from;
	private final IncPacket packet;
	private final short transactionID;

	public PacketReceivedEvent(Server from, IncPacket packet, short transactionID)
	{
		this.from = from;
		this.packet = packet;
		this.transactionID = transactionID;
	}

	public Server getFrom()
	{
		return from;
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
				"from=" + from +
				", packet=" + packet +
				", transactionID=" + transactionID +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PacketReceivedEvent that = (PacketReceivedEvent) o;

		return transactionID == that.transactionID && (from != null ? from.equals(that.from) : that.from == null &&
				(packet != null ? packet.equals(that.packet) : that.packet == null));

	}

	@Override
	public int hashCode()
	{
		int result = from != null ? from.hashCode() : 0;
		result = 31 * result + (packet != null ? packet.hashCode() : 0);
		result = 31 * result + (int) transactionID;
		return result;
	}
}
