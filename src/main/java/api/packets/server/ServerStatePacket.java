package api.packets.server;

import api.deployer.ServerState;
import api.packets.InPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataInput;
import java.io.IOException;

/**
 * Created by SkyBeast on 20/12/2016.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ServerStatePacket extends InPacket
{
	private final ServerState serverState;

	public ServerStatePacket(DataInput data) throws IOException
	{
		super(data);
		serverState = ServerState.values()[data.readUnsignedByte()];
	}
}
