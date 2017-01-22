package api.packets.players;

import api.packets.OutPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by loucass003 on 07/12/16.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SendPlayerDataPacket extends OutPacket
{
	private final UUID uuid;
	private final String rank;

	@Override
	public void write(DataOutputStream data) throws IOException
	{
		data.writeLong(uuid.getMostSignificantBits());
		data.writeLong(uuid.getLeastSignificantBits());
		data.writeUTF(rank);
	}
}
