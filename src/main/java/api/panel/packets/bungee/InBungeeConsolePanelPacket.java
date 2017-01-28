package api.panel.packets.bungee;

import api.packets.IncPacket;
import api.panel.IncPanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by loucass003 on 1/28/17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class InBungeeConsolePanelPacket extends IncPacket implements IncPanelPacket
{
	private final boolean listen;

	public InBungeeConsolePanelPacket(DataInputStream data) throws IOException
	{
		super(data);
		listen = data.readBoolean();
	}
}