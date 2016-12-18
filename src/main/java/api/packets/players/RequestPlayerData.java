package api.packets.players;

import api.packets.IncPacket;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by loucass003 on 07/12/16.
 */
public class RequestPlayerData extends IncPacket
{
	private String player;

	public RequestPlayerData(DataInputStream data) throws IOException
	{
		super(data);

		player = data.readUTF();
	}
}
