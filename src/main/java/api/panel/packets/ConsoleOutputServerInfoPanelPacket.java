package api.panel.packets;

import api.packets.OutPacket;
import api.panel.PanelPacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by SkyBeast on 06/01/2017.
 */
public class ConsoleOutputServerInfoPanelPacket extends OutPacket implements PanelPacket
{
	private final UUID serverUUID;
	private final String line;

	public ConsoleOutputServerInfoPanelPacket(UUID serverUUID, String line)
	{
		this.serverUUID = serverUUID;
		this.line = line;
	}

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeLong(serverUUID.getMostSignificantBits());
		out.writeLong(serverUUID.getLeastSignificantBits());
		out.writeUTF(line);
	}

	public UUID getServerUUID()
	{
		return serverUUID;
	}

	public String getLine()
	{
		return line;
	}

	@Override
	public String toString()
	{
		return "ConsoleOutputServerInfoPanelPacket{" +
				"serverUUID=" + serverUUID +
				", line='" + line + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ConsoleOutputServerInfoPanelPacket that = (ConsoleOutputServerInfoPanelPacket) o;

		return serverUUID != null ? serverUUID.equals(that.serverUUID) : that.serverUUID == null && (line != null ?
				line.equals(that.line) : that.line == null);

	}

	@Override
	public int hashCode()
	{
		int result = serverUUID != null ? serverUUID.hashCode() : 0;
		result = 31 * result + (line != null ? line.hashCode() : 0);
		return result;
	}
}
