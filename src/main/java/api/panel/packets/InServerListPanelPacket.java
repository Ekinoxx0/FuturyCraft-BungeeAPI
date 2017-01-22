package api.panel.packets;

import api.packets.IncPacket;
import api.panel.PanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 05/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class InServerListPanelPacket extends IncPacket implements PanelPacket
{
	private final boolean listen;

	public InServerListPanelPacket(DataInputStream data) throws IOException
	{
		super(data);
		listen = data.readBoolean();
	}
}
