package api.panel.packets;

import api.packets.IncPacket;
import api.panel.PanelPacket;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 05/01/2017.
 */
public class InServerListPanelPacket extends IncPacket implements PanelPacket
{
	private final boolean listen;

	public InServerListPanelPacket(DataInputStream data, boolean listen) throws IOException
	{
		super(data);
		this.listen = listen;
	}

	public boolean isListen()
	{
		return listen;
	}

	@Override
	public String toString()
	{
		return "InServerListPanelPacket{" +
				"listen=" + listen +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		InServerListPanelPacket that = (InServerListPanelPacket) o;

		return listen == that.listen;
	}

	@Override
	public int hashCode()
	{
		return (listen ? 1 : 0);
	}
}
