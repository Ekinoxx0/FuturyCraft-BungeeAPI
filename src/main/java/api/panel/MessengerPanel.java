package api.panel;

import api.Main;
import api.events.PanelPacketReceivedEvent;
import api.packets.IncPacket;
import api.packets.MessengerClient;
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
		Main.getInstance().getPanelManager().setMessengerPanel(this);
	}

	@Override
	protected void handleData(byte id, short transactionID, byte[] arrayIn) throws IOException
	{
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(arrayIn)); //Create an InputStream
		// from the byte array, so it can be redistributed

		try
		{
			IncPacket packet = PanelPackets.constructIncomingPacket(id, data);

			if (packet == null)
				throw new IllegalArgumentException("Cannot find packet ID " + id + " (transactionID=" + transactionID
						+ ", in=" + Arrays.toString(arrayIn) + ')');


			ProxyServer.getInstance().getPluginManager().callEvent(new PanelPacketReceivedEvent(server, packet,
					transactionID));
		}
		catch (ReflectiveOperationException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while constructing packet id " + id + " with " +
					"data " + Arrays.toString(arrayIn) + " (Client: " + this + ')', e);
		}
	}
}
