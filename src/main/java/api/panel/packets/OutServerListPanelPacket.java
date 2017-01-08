package api.panel.packets;

import api.data.Server;
import api.deployer.Lobby;
import api.packets.OutPacket;
import api.packets.server.ServerStatePacket;
import api.panel.PanelPacket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by SkyBeast on 05/01/2017.
 */
public class OutServerListPanelPacket extends OutPacket implements PanelPacket
{
	private final List<ServerData> servers;

	public OutServerListPanelPacket(List<ServerData> servers)
	{
		this.servers = servers;
	}

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeShort(servers.size());
		for (ServerData data : servers)
		{
			out.writeLong(data.uuid.getMostSignificantBits());
			out.writeLong(data.uuid.getLeastSignificantBits());
			out.writeUTF(data.name);
			out.writeShort(data.online);
			out.writeShort(data.maxOnline);
			out.writeShort(data.offset);
			out.writeByte(data.state.ordinal());
			out.writeUTF(data.serverType);
			out.writeUTF(data.category);
		}
	}

	public List<ServerData> getServers()
	{
		return servers;
	}

	@Override
	public String toString()
	{
		return "OutServerListPanelPacket{" +
				"servers=" + servers +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OutServerListPanelPacket that = (OutServerListPanelPacket) o;

		return servers != null ? servers.equals(that.servers) : that.servers == null;

	}

	@Override
	public int hashCode()
	{
		return servers != null ? servers.hashCode() : 0;
	}

	public static class ServerData
	{
		private final UUID uuid;
		private final String name;
		private final short online;
		private final short maxOnline;
		private final short offset;
		private final ServerStatePacket.ServerState state;
		private final String serverType;
		private final String category;

		public ServerData(UUID uuid, String name, short online, short maxOnline, short offset, ServerStatePacket
				.ServerState state, String serverType, String category)
		{
			this.uuid = uuid;
			this.name = name;
			this.online = online;
			this.maxOnline = maxOnline;
			this.offset = offset;
			this.state = state;
			this.serverType = serverType;
			this.category = category;
		}

		public static ServerData from(Server server)
		{
			return new ServerData(server.getUUID(),
					server.getName(),
					(short) server.getInfo().getPlayers().size(),
					(short) server.getDeployer().getVariant().getSlots(),
					(short) server.getOffset(),
					server.getServerState(),
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

		public String getServerType()
		{
			return serverType;
		}

		public String getCategory()
		{
			return category;
		}

		public short getMaxOnline()
		{
			return maxOnline;
		}

		@Override
		public String toString()
		{
			return "ServerData{" +
					"uuid=" + uuid +
					", name='" + name + '\'' +
					", online=" + online +
					", maxOnline=" + maxOnline +
					", offset=" + offset +
					", state=" + state +
					", serverType='" + serverType + '\'' +
					", category='" + category + '\'' +
					'}';
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ServerData that = (ServerData) o;

			return online == that.online && maxOnline == that.maxOnline && offset == that.offset && (uuid != null ?
					uuid.equals(that.uuid) : that.uuid == null && (name != null ? name.equals(that.name) : that.name
					== null && state == that.state && (serverType != null ? serverType.equals(that.serverType) : that
					.serverType == null && (category != null ? category.equals(that.category) : that.category == null)
			)));

		}

		@Override
		public int hashCode()
		{
			int result = uuid != null ? uuid.hashCode() : 0;
			result = 31 * result + (name != null ? name.hashCode() : 0);
			result = 31 * result + (int) online;
			result = 31 * result + (int) maxOnline;
			result = 31 * result + (int) offset;
			result = 31 * result + (state != null ? state.hashCode() : 0);
			result = 31 * result + (serverType != null ? serverType.hashCode() : 0);
			result = 31 * result + (category != null ? category.hashCode() : 0);
			return result;
		}
	}
}
