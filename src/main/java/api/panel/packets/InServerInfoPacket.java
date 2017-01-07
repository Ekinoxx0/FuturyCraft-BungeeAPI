package api.panel.packets;

import api.packets.IncPacket;
import api.panel.PanelPacket;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by SkyBeast on 06/01/2017.
 */
public class InServerInfoPacket extends IncPacket implements PanelPacket
{
	private final UUID uuid;
	private final boolean listen;

	public InServerInfoPacket(DataInputStream data, UUID uuid, boolean listen) throws IOException
	{
		super(data);
		this.uuid = uuid;
		this.listen = listen;
	}

	public boolean isListen()
	{
		return listen;
	}

	public UUID getUUID()
	{
		return uuid;
	}

	@Override
	public String toString()
	{
		return "InServerInfoPacket{" +
				"uuid=" + uuid +
				", listen=" + listen +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		InServerInfoPacket that = (InServerInfoPacket) o;

		return listen == that.listen && (uuid != null ? uuid.equals(that.uuid) : that.uuid == null);
	}

	@Override
	public int hashCode()
	{
		int result = uuid != null ? uuid.hashCode() : 0;
		result = 31 * result + (listen ? 1 : 0);
		return result;
	}
}
