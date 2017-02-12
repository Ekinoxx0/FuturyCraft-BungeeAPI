package api.packets.server;

import api.deployer.ServerState;
import api.packets.IncPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 20/12/2016.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ServerStatePacket extends IncPacket
{
	private final ServerState serverState;

	public ServerStatePacket(DataInputStream data) throws IOException
	{
		super(data);
		serverState = ServerState.values()[data.readUnsignedByte()];
	}
}
