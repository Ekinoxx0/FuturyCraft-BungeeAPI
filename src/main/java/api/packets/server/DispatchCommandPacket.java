package api.packets.server;

import api.packets.OutPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 05/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DispatchCommandPacket extends OutPacket
{
	private final String command;

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeUTF(command);
	}
}
