package api.packets;

import api.packets.players.RequestPlayerData;
import api.packets.players.SendPlayerData;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by loucass003 on 07/12/16.
 */

public enum Packets
{
	REQUEST_PLAYER_DATA((byte) 0x00, RequestPlayerData.class),
	SEND_PLAYER_DATA((byte) 0x01, SendPlayerData.class);

	private final byte id;
	private final Class<? extends Packet> clazz;

	Packets(byte id, Class<? extends Packet> clazz)
	{
		this.id = id;
		this.clazz = clazz;
	}

	static Packet constructPacket(byte id, DataInputStream dis)
			throws IOException, ReflectiveOperationException
	{
		for (Packets p : values())
			if (id == p.id)
				return p.clazz.getConstructor(DataInputStream.class).newInstance(dis);
		return null;
	}

	static byte getID(Class<? extends Packet> clazz)
	{
		for (Packets p : values())
			if (clazz == p.clazz)
				return p.id;
		throw new IllegalArgumentException("ID not found"); //Should never happen
	}
}
