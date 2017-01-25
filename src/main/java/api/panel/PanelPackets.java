package api.panel;

import api.panel.packets.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 04/01/2017.
 */
@AllArgsConstructor
@Getter
public enum PanelPackets
{
	// OUTGOING - Panel-bound
	OUT_HEADER((byte) 0x00, false, OutHeaderPanelPacket.class),
	OUT_SERVER_LIST((byte) 0x01, false, OutServerListPanelPacket.class),
	ADD_SERVER_LIST((byte) 0x02, false, AddServerListPanelPacket.class),
	REMOVE_SERVER_LIST((byte) 0x03, false, RemoveServerListPanelPacket.class),
	UPDATE_SERVER_LIST((byte) 0x04, false, UpdateServerListPanelPacket.class),
	OUT_SERVER_INFO((byte) 0x05, false, OutServerInfoPanelPacket.class),
	ILLEGAL_SERVER_INFO((byte) 0x06, false, IllegalServerInfoPacket.class),
	CONSOLE_OUTPUT((byte) 0x07, false, ConsoleOutputServerInfoPanelPacket.class),

	// INCOMING - Bungee-bound
	IN_HEADER((byte) 0x00, true, InHeaderPanelPacket.class),
	IN_SERVER_LIST((byte) 0x01, true, InServerListPanelPacket.class),
	IN_SERVER_INFO((byte) 0x05, true, InServerInfoPanelPacket.class),
	CONSOLE_INPUT((byte) 0x07, true, ConsoleInputServerInfoPanelPacket.class);

	private final byte id;
	private final boolean in;
	private final Class<? extends PanelPacket> clazz;

	static IncPanelPacket constructIncomingPacket(byte id, DataInputStream dis)
			throws IOException, ReflectiveOperationException
	{
		for (PanelPackets p : values())
			if (p.in && id == p.id)
				return (IncPanelPacket) p.clazz.getConstructor(DataInputStream.class).newInstance(dis);
		return null;
	}

	static byte getId(Class<? extends PanelPacket> clazz)
	{
		for (PanelPackets p : values())
			if (clazz == p.clazz)
				return p.id;
		throw new IllegalArgumentException("ID not found"); //Should never happen
	}
}
