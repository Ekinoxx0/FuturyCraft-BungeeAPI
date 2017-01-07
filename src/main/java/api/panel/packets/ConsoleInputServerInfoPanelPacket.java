package api.panel.packets;

import api.packets.IncPacket;
import api.panel.PanelPacket;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 06/01/2017.
 */
public class ConsoleInputServerInfoPanelPacket extends IncPacket implements PanelPacket
{
	private final String in;

	public ConsoleInputServerInfoPanelPacket(DataInputStream data) throws IOException
	{
		super(data);
		this.in = data.readUTF();
	}

	public String getIn()
	{
		return in;
	}

	@Override
	public String toString()
	{
		return "ConsoleInputServerInfoPanelPacket{" +
				"in='" + in + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ConsoleInputServerInfoPanelPacket that = (ConsoleInputServerInfoPanelPacket) o;

		return in != null ? in.equals(that.in) : that.in == null;

	}

	@Override
	public int hashCode()
	{
		return in != null ? in.hashCode() : 0;
	}
}
