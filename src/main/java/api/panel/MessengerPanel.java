package api.panel;

import api.Main;
import api.events.PanelPacketReceivedEvent;
import api.packets.MessengerClient;
import api.packets.Packet;
import net.md_5.bungee.api.ProxyServer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 04/01/2017.
 */
public class MessengerPanel extends MessengerClient
{
	public MessengerPanel(Socket socket, DataInputStream in, DataOutputStream out) throws IOException
	{
		super(socket, in, out, null);
	}

	public void register()
	{
		Main.getInstance().getPanelManager().setMessengerPanel(this);
	}

	@Override
	protected void handleData(byte id, short transactionID, byte[] arrayIn) throws IOException
	{
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(arrayIn));
		try
		{
			IncPanelPacket packet = PanelPackets.constructIncomingPacket(id, data);

			if (packet == null)
				throw new IllegalArgumentException("Cannot find packet ID " + id + " (transactionID=" + transactionID
						+ ", in=" + Arrays.toString(arrayIn) + ')');

			System.out.println(packet + " " + transactionID);
			ProxyServer.getInstance().getPluginManager().callEvent(new PanelPacketReceivedEvent(server, packet,
					transactionID));
		}
		catch (ReflectiveOperationException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while constructing packet id " + id + " with " +
					"data " + Arrays.toString(arrayIn) + " (Client: " + this + ')', e);
		}
	}

	@Override
	protected byte getPacketID(Packet packet)
	{
		return PanelPackets.getId(((PanelPacket) packet).getClass());
	}

	@Override
	protected void unregister()
	{
		Main.getInstance().getPanelManager().setMessengerPanel(null);
		Main.getInstance().getPanelManager().getListener().resetListening();
	}
}
