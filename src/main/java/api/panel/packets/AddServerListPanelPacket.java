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
import java.util.UUID;

/**
 * Created by SkyBeast on 06/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AddServerListPanelPacket extends OutPacket implements OutPanelPacket
{
	private final UUID uuid;
	private final String name;
	private final short online;
	private final short maxPlayers;
	private final short offset;
	private final ServerStatePacket.ServerState state;
	private final String serverType;
	private final String category;

	@Override
	public void write(DataOutputStream out) throws IOException
	{
		out.writeLong(uuid.getMostSignificantBits());
		out.writeLong(uuid.getLeastSignificantBits());
		out.writeUTF(name);
		out.writeShort(online);
		out.writeShort(maxPlayers);
		out.writeShort(offset);
		out.writeByte(state.ordinal());
		out.writeUTF(serverType);
		out.writeUTF(category);
	}

	public static AddServerListPanelPacket from(Server server)
	{
		return new AddServerListPanelPacket(
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
}
