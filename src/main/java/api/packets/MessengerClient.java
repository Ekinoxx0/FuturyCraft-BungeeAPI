package api.packets;

import api.Main;
import api.data.Server;
import api.events.PacketReceivedEvent;
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
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final List<PacketListener<?>> listeners = new ArrayList<>();
	protected Server server;
	private short lastTransactionID = 0x0000;
	private Thread listener;
	private volatile boolean end = false;

	public MessengerClient(Socket socket, DataInputStream in, DataOutputStream out, Server server) throws IOException
	//Called in DeployerServer
	// identifier thread
	{
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.server = server;
		setupSocketListener();
	}

	private void setupSocketListener() //Called in DeployerServer connection listener
	{
		listener = new Thread(() ->
		{
			while (!socket.isClosed())
			{
				try
				{
					byte id = in.readByte();
					short transactionID = in.readShort();
					int size = in.readUnsignedShort();
					byte[] data = new byte[size];
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
	protected void handleData(byte id, short transactionID, byte[] arrayIn) throws IOException
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

	public <T extends IncPacket> void listenPacket(Class<T> clazz, int transactionID, Callback<T> callback)
	{
		synchronized (listeners)
		{
			listeners.add(new PacketListener<>(clazz, callback, transactionID));
		}
	}

	public short sendPacket(OutPacket packet)
	{
		short transactionID = lastTransactionID++;
		sendPacket(packet, transactionID);
		return transactionID;
	}

	public void sendPacket(OutPacket packet, short transactionID)
	{
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(array);

		try
		{
			packet.write(data);

			synchronized (out)
			{
				Main.getInstance().getLogger().info("Sending " + packet + " in client " + this);
				out.writeByte(Packets.getID(packet.getClass()));
				out.writeShort(transactionID);
				out.writeShort(array.size());
				out.write(array.toByteArray());
				out.flush();
			}
		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while sending packet " + packet + " with " +
					"transactionID " + transactionID + " (Client: " + this + ")", e);
		}
	}

	public void disconnect()
	{
		Main.getInstance().getMessenger().unregister(this);
		end = true;
		try
		{
			socket.close();
		}
		catch (IOException ignored) {}
	}

	Server getServer()
	{
		return server;
	}

	@Override
	public String toString()
	{
		return "MessengerClient{" +
				"socket=" + socket +
				", in=" + in +
				", out=" + out +
				", listeners=" + listeners +
				", lastTransactionID=" + lastTransactionID +
				", listener=" + listener +
				", end=" + end +
				'}';
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
}
