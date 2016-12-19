package api.packets.players;

import api.packets.OutPacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by loucass003 on 07/12/16.
 */
public class SendPlayerDataPacket extends OutPacket
{
	private final UUID uuid;
	private final String rank;

	public SendPlayerDataPacket(UUID uuid, String rank)
	{
		this.uuid = uuid;
		this.rank = rank;
	}

	public UUID getUuid()
	{
		return uuid;
	}

	public String getRank()
	{
		return rank;
	}

	@Override
	public void write(DataOutputStream data) throws IOException
	{
		data.writeLong(uuid.getMostSignificantBits());
		data.writeLong(uuid.getLeastSignificantBits());
		data.writeUTF(rank);
	}
}
