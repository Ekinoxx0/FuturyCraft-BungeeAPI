package api.packets;

import api.packets.players.EndGameDataPacket;
import api.packets.server.BossBarMessagesPacket;
import api.packets.server.RequestBossBarMessagesPacket;
import api.packets.server.ServerStatePacket;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.DataInput;
import java.io.IOException;

/**
 * Created by loucass003 on 07/12/16.
 */
@AllArgsConstructor
@Getter
public enum Packets
{
	// OUTGOING - Spigot-bound
	BB_MESSAGES((byte) 0x00, false, BossBarMessagesPacket.class),

	// INCOMING - Bungee-bound
	SERVER_STATE((byte) 0x00, true, ServerStatePacket.class),
	IN_BB_MESSAGES((byte) 0x01, true, RequestBossBarMessagesPacket.class),
	END_GAME_DATA((byte) 0x02, true, EndGameDataPacket.class);

	private final byte id;
	private final boolean in;
	private final Class<? extends Packet> clazz;

	static InPacket constructIncomingPacket(byte id, DataInput dis)
			throws IOException, ReflectiveOperationException
	{
		for (Packets p : values())
			if (p.in && id == p.id)
				return (InPacket) p.clazz.getConstructor(DataInput.class).newInstance(dis);
		return null;
	}

	static byte getId(Class<? extends Packet> clazz)
	{
		for (Packets p : values())
			if (clazz == p.clazz)
				return p.id;
		throw new IllegalArgumentException("ID not found"); //Should never happen
	}
}
