package api.panel.packets.servers;

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
		for(ServerData data : servers)
			data.write(out);
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
							"Game"
			);
		}

		@SuppressWarnings("Duplicates")
		public void write(DataOutputStream out) throws IOException
		{
			out.writeLong(uuid.getMostSignificantBits());
			out.writeLong(uuid.getLeastSignificantBits());
			out.writeUTF(name);
			out.writeShort(online);
			out.writeShort(maxOnline);
			out.writeShort(offset);
			out.writeByte(state.ordinal());
			out.writeUTF(serverType);
			out.writeUTF(category);
		}
	}
}
