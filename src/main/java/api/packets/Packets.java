package api.packets;

import api.packets.players.RequestPlayerData;
import api.packets.players.RequestTPS;
import api.packets.players.SendPlayerData;
import api.packets.players.SendTPS;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by loucass003 on 07/12/16.
 */
public enum Packets
{
	// INCOMING - Bungee-bound
	REQUEST_PLAYER_DATA((byte) 0x00, true, RequestPlayerData.class),
	REQUEST_TPS((byte) 0x01, true, RequestTPS.class),

	// OUTGOING - Spigot-bound
	SEND_PLAYER_DATA((byte) 0x00, false, SendPlayerData.class),
	SEND_TPS((byte) 0x01, false, SendTPS.class);

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

	public byte getId()
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
