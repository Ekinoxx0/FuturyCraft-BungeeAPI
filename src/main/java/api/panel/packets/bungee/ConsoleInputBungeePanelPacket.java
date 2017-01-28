package api.panel.packets.bungee;

import api.packets.IncPacket;
import api.panel.IncPanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by SkyBeast on 06/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ConsoleInputBungeePanelPacket extends IncPacket implements IncPanelPacket
{
	private final String in;

	public ConsoleInputBungeePanelPacket(DataInputStream data) throws IOException
	{
		super(data);
		in = data.readUTF();
	}
}
