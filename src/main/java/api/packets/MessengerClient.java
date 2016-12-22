package api.packets;

import api.Main;
import api.data.Server;
import net.md_5.bungee.api.ProxyServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 17/12/2016.
 */
public class MessengerClient
{
	private final MessengerServer messengerServer;
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final List<PacketListener<?>> listeners = new ArrayList<>();
	private short lastTransactionID = 0x0000;
	private Thread listener;
	private volatile boolean end = false;
	private Server server;

	MessengerClient(Socket socket, MessengerServer messengerServer) throws IOException //Called in DeployerServer connection
	// listener
	{
		this.messengerServer = messengerServer;
		this.socket = socket;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		setupSocketListener();
	}

	private void setupSocketListener() //Called in DeployerServer connection listener
	{
		listener = new Thread(() ->
		{
			try
			{
				int port = in.readInt(); //Identify

				if (!identify(port))
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "The socket sent a non-registered port for " +
							"identification! Disconnecting it... (Client: " + this + ")");
					disconnect(); //Disconnect
					return; //Don't run the infinite loop
				}

				out.writeBoolean(true);
				out.flush();
			}
			catch (Exception e)
			{
				Main.getInstance().getLogger().log(Level.SEVERE, "Error while reading the identifier (Client: " + this
								+ ")",
						e);
				return;
			}

			//Socket identified

			while (!socket.isClosed())
			{
				try
				{
					byte id = in.readByte();
					short transactionID = in.readShort();
					byte[] data = new byte[in.readShort()];
					in.readFully(data); //Read all data and store it to the array

					handleData(id, transactionID, data);
				}
				catch (IOException e)
				{
					if (!end)
						Main.getInstance().getLogger().log(Level.SEVERE, "Error while reading a command (Client: " +
										this + ")",
								e);
				}
				catch (Exception e)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "Error while reading the socket (Client: " +
									this + ")",
							e);
				}
			}
		}
		);

		listener.start();
	}

	@SuppressWarnings("unchecked")
	private void handleData(byte id, short transactionID, byte[] arrayIn) throws IOException
	{
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(arrayIn)); //Create an InputStream
		// from the byte array, so it can be redistributed

		try
		{
			IncPacket packet = Packets.constructIncomingPacket(id, data);

			if (packet == null)
				throw new IllegalArgumentException("Cannot find packet ID " + id + " (transactionID=" + transactionID
						+ ", in=" + Arrays.toString(arrayIn) + ")");

			synchronized (listeners)
			{
				listeners.forEach(listener ->
				{
					if (listener.transactionID == transactionID && listener.clazz == packet.getClass())
					{
						((Callback<IncPacket>) listener.callback).response(packet);
						listeners.remove(listener);
					}
				});
			}

			ProxyServer.getInstance().getPluginManager().callEvent(new PacketReceivedEvent(server, packet,
					transactionID));
		}
		catch (ReflectiveOperationException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while constructing packet id " + id + " with " +
					"data " + Arrays.toString(arrayIn) + " (Client: " + this + ")", e);
		}
	}

	private boolean identify(int port)
	{
		Server server = Main.getInstance().getDataManager().findServerByPort(port);
		if (server == null)
			return false;

		this.server = server;
		messengerServer.register(server, this);
		return true;
	}

	public <T extends IncPacket> void listenPacket(Class<T> clazz, int transactionID, Callback<T> callback)
	{
		synchronized (listeners)
		{
			listeners.add(new PacketListener<>(clazz, callback, transactionID));
		}
	}

	public short sendPacket(OutPacket packet) throws IOException
	{
		short transactionID = lastTransactionID++;
		sendPacket(packet, transactionID);
		return transactionID;
	}

	public void sendPacket(OutPacket packet, short transactionID) throws IOException
	{
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(new ByteArrayOutputStream());
		packet.write(data);

		synchronized (out)
		{
			out.write(Packets.getID(packet.getClass()));
			out.write(transactionID);
			out.write(array.size());
			out.write(array.toByteArray());
			out.flush();
		}
	}

	public void disconnect()
	{
		messengerServer.unregister(this);
		end = true;
		try
		{
			socket.close();
		}
		catch (IOException ignored) {}
	}

	private class PacketListener<T extends IncPacket>
	{
		Class<T> clazz;
		Callback<T> callback;
		int transactionID;

		PacketListener(Class<T> clazz, Callback<T> callback, int transactionID)
		{
			this.clazz = clazz;
			this.callback = callback;
			this.transactionID = transactionID;
		}

		@Override
		public String toString()
		{
			return "PacketListener{" +
					"clazz=" + clazz +
					", callback=" + callback +
					", transactionID=" + transactionID +
					'}';
		}
	}

	@Override
	public String toString()
	{
		return "MessengerClient{" +
				"messengerServer=" + messengerServer +
				", socket=" + socket +
				", in=" + in +
				", out=" + out +
				", listeners=" + listeners +
				", lastTransactionID=" + lastTransactionID +
				", listener=" + listener +
				", end=" + end +
				'}';
	}
}
