package api.panel.packets;

import api.data.Server;
import api.deployer.Lobby;
import api.packets.OutPacket;
import api.packets.server.ServerStatePacket;
import api.panel.PanelPacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by SkyBeast on 06/01/2017.
 */
public class OutServerInfoPanelPacket extends OutPacket implements PanelPacket
{
	private final UUID uuid;
	private final String name;
	private final short online;
	private final short offset;
	private final ServerStatePacket.ServerState state;
	private final String serverType;
	private final String category;
	private final String console;

	public OutServerInfoPanelPacket(UUID uuid, String name, short online, short offset, ServerStatePacket.ServerState
			state, String serverType, String category, String console)
	{
		this.uuid = uuid;
		this.name = name;
		this.online = online;
		this.offset = offset;
		this.state = state;
		this.serverType = serverType;
		this.category = category;
		this.console = console;
	}

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeLong(uuid.getMostSignificantBits());
		out.writeLong(uuid.getLeastSignificantBits());
		out.writeUTF(name);
		out.writeShort(online);
		out.writeShort(offset);
		out.writeByte(state.ordinal());
		out.writeUTF(console);
	}

	public static OutServerInfoPanelPacket from(Server server, boolean console)
	{
		return new OutServerInfoPanelPacket(server.getUUID(),
				server.getName(),
				(short) server.getInfo().getPlayers().size(),
				(short) server.getOffset(),
				server.getServerState(),
				server.getDeployer().getConsole(),
				server.getDeployer().getType().toString(),
				server.getDeployer() instanceof Lobby ? ((Lobby) server.getDeployer()).getLobbyType().toString() :
						"Game");
	}

	public UUID getUUID()
	{
		return uuid;
	}

	public String getName()
	{
		return name;
	}

	public short getOnline()
	{
		return online;
	}

	public short getOffset()
	{
		return offset;
	}

	public ServerStatePacket.ServerState getState()
	{
		return state;
	}

	public String getConsole()
	{
		return console;
	}

	public String getServerType()
	{
		return serverType;
	}

	public String getCategory()
	{
		return category;
	}

	@Override
	public String toString()
	{
		return "OutServerInfoPanelPacket{" +
				"uuid=" + uuid +
				", name='" + name + '\'' +
				", online=" + online +
				", offset=" + offset +
				", state=" + state +
				", serverType='" + serverType + '\'' +
				", category='" + category + '\'' +
				", console='" + console + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OutServerInfoPanelPacket that = (OutServerInfoPanelPacket) o;

		return online == that.online && offset == that.offset && (uuid != null ? uuid.equals(that.uuid) : that.uuid ==
				null && (name != null ? name.equals(that.name) : that.name == null && state == that.state &&
				(serverType != null ? serverType.equals(that.serverType) : that.serverType == null && (category !=
						null ? category.equals(that.category) : that.category == null && (console != null ? console
						.equals(that.console) : that.console == null)))));

	}

	@Override
	public int hashCode()
	{
		int result = uuid != null ? uuid.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (int) online;
		result = 31 * result + (int) offset;
		result = 31 * result + (state != null ? state.hashCode() : 0);
		result = 31 * result + (serverType != null ? serverType.hashCode() : 0);
		result = 31 * result + (category != null ? category.hashCode() : 0);
		result = 31 * result + (console != null ? console.hashCode() : 0);
		return result;
	}
}
