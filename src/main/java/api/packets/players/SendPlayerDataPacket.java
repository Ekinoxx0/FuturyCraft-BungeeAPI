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

	public UUID getUUID()
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

	@Override
	public String toString()
	{
		return "SendPlayerDataPacket{" +
				"uuid=" + uuid +
				", rank='" + rank + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SendPlayerDataPacket that = (SendPlayerDataPacket) o;

		return uuid != null ? uuid.equals(that.uuid) : that.uuid == null && (rank != null ? rank.equals(that.rank) :
				that.rank == null);

	}

	@Override
	public int hashCode()
	{
		int result = uuid != null ? uuid.hashCode() : 0;
		result = 31 * result + (rank != null ? rank.hashCode() : 0);
		return result;
	}
}
