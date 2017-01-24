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
public class OutServerInfoPanelPacket extends OutPacket implements OutPanelPacket
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
	private final long minMemory;
	private final long freeMemory;
	private final long totalMemory;
	private final long maxMemory;
	private final float processCpuLoad;
	private final byte[] lastTPS;
	private final String console;

	@Override
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
		out.writeLong(lastKeepAlive);
		out.writeLong(minMemory);
		out.writeLong(freeMemory);
		out.writeLong(totalMemory);
		out.writeLong(maxMemory);
		out.writeFloat(processCpuLoad);
		out.write(lastTPS);
		out.writeUTF(console);
	}

	public static OutServerInfoPanelPacket from(Server server, boolean console)
	{
		return new OutServerInfoPanelPacket(
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
				server.getMinMemory(),
				server.getFreeMemory(),
				server.getTotalMemory(),
				server.getMaxMemory(),
				server.getProcessCpuLoad(),
				server.getLastTPS(),
				console ? server.getDeployer().getConsole() : ""
		);
	}
}
