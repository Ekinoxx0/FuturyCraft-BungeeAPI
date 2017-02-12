package api.panel;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 04/01/2017.
 */
@AllArgsConstructor
@Getter
public enum PanelPackets
{
	;

	private final byte id;
	private final boolean in;
	private final Class<? extends PanelPacket> clazz;

	static IncPanelPacket constructIncomingPacket(byte id, DataInputStream dis)
			throws IOException, ReflectiveOperationException
	{
		for (PanelPackets p : values())
			if (p.in && id == p.id)
				return (IncPanelPacket) p.clazz.getConstructor(DataInputStream.class).newInstance(dis);
		return null;
	}

	static byte getId(Class<? extends PanelPacket> clazz)
	{
		for (PanelPackets p : values())
			if (clazz == p.clazz)
				return p.id;
		throw new IllegalArgumentException("ID not found"); //Should never happen
	}
}
