package api.packets;

import api.packets.players.SendPlayerDataPacket;
import api.packets.server.DispatchCommandPacket;
import api.packets.server.KeepAlivePacket;
import api.packets.server.ServerStatePacket;
import api.packets.server.StopPacket;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by loucass003 on 07/12/16.
 */
public enum Packets
{
	// OUTGOING - Spigot-bound
	SEND_PLAYER_DATA((byte) 0x00, false, SendPlayerDataPacket.class),
	REQUEST_STOP((byte) 0x01, false, StopPacket.class),
	DISPATCH_COMMAND((byte) 0x02, false, DispatchCommandPacket.class),

	// INCOMING - Bungee-bound
	KEEP_ALIVE((byte) 0x00, true, KeepAlivePacket.class),
	SERVER_STATE((byte) 0x01, true, ServerStatePacket.class);

	private final byte id;
	private final boolean in;
	private final Class<? extends Packet> clazz;

	Packets(byte id, boolean in, Class<? extends Packet> clazz)
	{
		this.id = id;
		this.in = in;
		this.clazz = clazz;
	}

	static IncPacket constructIncomingPacket(byte id, DataInputStream dis)
			throws IOException, ReflectiveOperationException
	{
		for (Packets p : values())
			if (p.in && id == p.id)
				return (IncPacket) p.clazz.getConstructor(DataInputStream.class).newInstance(dis);
		return null;
	}

	static byte getID(Class<? extends Packet> clazz)
	{
		for (Packets p : values())
			if (clazz == p.clazz)
				return p.id;
		throw new IllegalArgumentException("ID not found"); //Should never happen
	}

	public byte getID()
	{
		return id;
	}

	public boolean isServerBound()
	{
		return in;
	}

	public Class<? extends Packet> getPacketClass()
	{
		return clazz;
	}
}
