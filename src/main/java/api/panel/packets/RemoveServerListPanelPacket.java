package api.panel.packets;

import api.packets.OutPacket;
import api.panel.PanelPacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by SkyBeast on 06/01/2017.
 */
public class RemoveServerListPanelPacket extends OutPacket implements PanelPacket
{
	private final UUID uuid;

	public RemoveServerListPanelPacket(UUID uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeLong(uuid.getMostSignificantBits());
		out.writeLong(uuid.getLeastSignificantBits());
	}

	public UUID getUuid()
	{
		return uuid;
	}

	@Override
	public String toString()
	{
		return "RemoveServerListPanelPacket{" +
				"uuid=" + uuid +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RemoveServerListPanelPacket that = (RemoveServerListPanelPacket) o;

		return uuid != null ? uuid.equals(that.uuid) : that.uuid == null;

	}

	@Override
	public int hashCode()
	{
		return uuid != null ? uuid.hashCode() : 0;
	}
}
