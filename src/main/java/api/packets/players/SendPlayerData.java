package api.packets.players;

import api.packets.OutPacket;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by loucass003 on 07/12/16.
 */
public class SendPlayerData extends OutPacket
{
	private final String player;

	public SendPlayerData(String player)
	{
		this.player = player;
	}

	@Override
	public void write(DataOutputStream data) throws IOException
	{
		data.writeUTF(player);
	}
}
