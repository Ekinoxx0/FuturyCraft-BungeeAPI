package api.panel;

import api.packets.IncPacket;
import api.panel.packets.*;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by SkyBeast on 04/01/2017.
 */
public enum PanelPackets
{
	// OUTGOING - Panel-bound
	OUT_HEADER((byte) 0x00, false, OutHeaderPanelPacket.class),
	OUT_SERVER_LIST((byte) 0x01, false, OutServerListPanelPacket.class),
	ADD_SERVER_LIST((byte) 0x02, false, AddServerListPanelPacket.class),
	REMOVE_SERVER_LIST((byte) 0x03, false, RemoveServerListPanelPacket.class),
	UPDATE_SERVER_LIST((byte) 0x04, false, UpdateServerListPanelPacket.class),

	// INCOMING - Bungee-bound
	IN_HEADER((byte) 0x00, true, InHeaderPanelPacket.class),
	IN_SERVER_LIST((byte) 0x01, true, InServerListPanelPacket.class);

	private final byte id;
	private final boolean in;
	private final Class<? extends PanelPacket> clazz;

	PanelPackets(byte id, boolean in, Class<? extends PanelPacket> clazz)
	{
		this.id = id;
		this.in = in;
		this.clazz = clazz;
	}

	static IncPacket constructIncomingPacket(byte id, DataInputStream dis)
			throws IOException, ReflectiveOperationException
	{
		for (PanelPackets p : values())
			if (p.in && id == p.id)
				return (IncPacket) p.clazz.getConstructor(DataInputStream.class).newInstance(dis);
		return null;
	}

	static byte getID(Class<? extends PanelPacket> clazz)
	{
		for (PanelPackets p : values())
			if (clazz == p.clazz)
				return p.id;
		throw new IllegalArgumentException("ID not found"); //Should never happen
	}

	public byte getID()
	{
		return id;
	}

	public boolean isServerBound()
	{
		return in;
	}

	public Class<? extends PanelPacket> getPacketClass()
	{
		return clazz;
	}
}
