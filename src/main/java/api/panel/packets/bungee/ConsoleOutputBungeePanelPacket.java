package api.panel.packets.bungee;

import api.packets.OutPacket;
import api.panel.OutPanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by SkyBeast on 06/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConsoleOutputBungeePanelPacket extends OutPacket implements OutPanelPacket
{
	private final String line;

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeUTF(line);
	}
}
