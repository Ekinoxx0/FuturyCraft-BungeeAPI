package api.packets;

import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 18/12/2016.
 */
public class PacketReceivedEvent extends Event
{
	private final IncPacket packet;

	public PacketReceivedEvent(IncPacket packet)
	{
		this.packet = packet;
	}

	public IncPacket getPacket()
	{
		return packet;
	}

	@Override
	public String toString()
	{
		return "PacketReceivedEvent{" +
				"packet=" + packet +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PacketReceivedEvent that = (PacketReceivedEvent) o;

		return packet != null ? packet.equals(that.packet) : that.packet == null;
	}

	@Override
	public int hashCode()
	{
		return packet != null ? packet.hashCode() : 0;
	}
}
