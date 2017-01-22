package api.panel.packets;

import api.data.Server;
import api.deployer.Lobby;
import api.packets.OutPacket;
import api.packets.server.ServerStatePacket;
import api.panel.OutPanelPacket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by SkyBeast on 05/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OutServerListPanelPacket extends OutPacket implements OutPanelPacket
{
	private final List<ServerData> servers;

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
			out.writeLong(data.lastKeepAlive);
			out.writeLong(data.freeMemory);
			out.writeLong(data.totalMemory);
			out.writeFloat(data.processCpuLoad);
			out.write(data.lastTPS);
		}
	}

	@Data
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
		private final long lastKeepAlive;
		private final long freeMemory;
		private final long totalMemory;
		private final float processCpuLoad;
		private final byte[] lastTPS;

		public static ServerData from(Server server)
		{
			return new ServerData(
					server.getUuid(),
					server.getName(),
					(short) server.getInfo().getPlayers().size(),
					(short) server.getDeployer().getVariant().getSlots(),
					(short) server.getOffset(),
					server.getServerState(),
					server.getDeployer().getType().toString(),
					server.getDeployer() instanceof Lobby ? ((Lobby) server.getDeployer()).getLobbyType().toString() :
							"Game",
					server.getLastKeepAlive(),
					server.getFreeMemory(),
					server.getTotalMemory(),
					server.getProcessCpuLoad(),
					server.getLastTPS()
			);
		}
	}
}
