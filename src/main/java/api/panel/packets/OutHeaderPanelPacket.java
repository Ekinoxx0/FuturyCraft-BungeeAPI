package api.panel.packets;

import api.packets.OutPacket;
import api.panel.PanelPacket;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 05/01/2017.
 */
public class OutHeaderPanelPacket extends OutPacket implements PanelPacket
{
	private final short online;
	private final short servers;

	public OutHeaderPanelPacket(short online, short servers)
	{
		this.online = online;
		this.servers = servers;
	}

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeShort(online);
		out.writeShort(servers);
	}

	public short getOnline()
	{
		return online;
	}

	public short getServers()
	{
		return servers;
	}

	@Override
	public String toString()
	{
		return "OutHeaderPanelPacket{" +
				"online=" + online +
				", servers=" + servers +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OutHeaderPanelPacket that = (OutHeaderPanelPacket) o;

		return online == that.online && servers == that.servers;
	}

	@Override
	public int hashCode()
	{
		int result = (int) online;
		result = 31 * result + (int) servers;
		return result;
	}
}
