package api.packets;

import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public class PacketSendEvent extends Event
{
	private final OutPacket packet;

	public PacketSendEvent(OutPacket packet)
	{
		this.packet = packet;
	}

	public OutPacket getPacket()
	{
		return packet;
	}

	@Override
	public String toString()
	{
		return "PacketSendEvent{" +
				"packet=" + packet +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PacketSendEvent that = (PacketSendEvent) o;

		return packet != null ? packet.equals(that.packet) : that.packet == null;
	}

	@Override
	public int hashCode()
	{
		return packet != null ? packet.hashCode() : 0;
	}
}
