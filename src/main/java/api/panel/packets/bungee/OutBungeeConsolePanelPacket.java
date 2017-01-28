package api.panel.packets.bungee;

import api.packets.OutPacket;
import api.panel.OutPanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 05/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OutBungeeConsolePanelPacket extends OutPacket implements OutPanelPacket
{
	private final String console;

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeUTF(console);
	}
}
