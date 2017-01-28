package api.packets;

import api.Main;
import api.data.Server;
import api.events.PacketReceivedEvent;
import api.utils.concurrent.ThreadLoop;
import api.utils.concurrent.ThreadLoops;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 17/12/2016.
 */
@ToString(exclude = {"server"})
public class MessengerClient
{
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final List<PacketListener<?>> listeners = new ArrayList<>();
	private final ExecutorService sendPacketPool = Executors.newSingleThreadExecutor();
	@Getter(AccessLevel.PACKAGE)
	protected Server server;
	private volatile short lastTransactionID;
	private final ThreadLoop listener = setupSocketListener();
	private volatile boolean end;

	public MessengerClient(Socket socket, DataInputStream in, DataOutputStream out, Server server) throws IOException
	//Called in MessengerClient identifier thread
	{
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.server = server;
		listener.start();
	}

	private ThreadLoop setupSocketListener() //Called in MessengerClient connection listener
	{
		return ThreadLoops.newConditionThreadLoop
				(
						() -> !socket.isClosed() && !end,
						() ->
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
									Main.getInstance().getLogger().log(Level.SEVERE, "Error while reading a command " +
											"(Client: " + this + ')', e);
								disconnect();
							}

							if (socket.isClosed())
								disconnect();
						}
				);
	}

	protected void handleDisconnection()
	{

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
						+ ", in=" + Arrays.toString(arrayIn) + ')');

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
					"data " + Arrays.toString(arrayIn) + " (Client: " + this + ')', e);
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
		sendPacketPool.execute(() -> internalSendPacket(packet, transactionID));
		return transactionID;
	}

	public void sendPacket(OutPacket packet, short transactionID)
	{
		sendPacketPool.execute(() -> internalSendPacket(packet, transactionID));
	}

	protected void internalSendPacket(OutPacket packet, short transactionID)
	{
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(array);

		try
		{
			packet.write(data);

			Main.getInstance().getLogger().info("Sending " + packet + " in client " + this + '.');
			out.writeByte(getPacketID(packet));
			out.writeShort(transactionID);
			out.writeShort(array.size());
			out.write(array.toByteArray());
			out.flush();

		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while sending packet " + packet + " with " +
					"transactionID " + transactionID + " (Client: " + this + ')', e);
		}
	}

	protected byte getPacketID(Packet packet)
	{
		return Packets.getId(packet.getClass());
	}

	public void disconnect()
	{
		unregister();
		end = true;
		listener.stop();
		try
		{
			socket.close();
		}
		catch (IOException ignored)
		{
		}

	}

	protected void unregister()
	{
		Main.getInstance().getMessenger().unregister(this);
	}

	@ToString
	@AllArgsConstructor
	private static class PacketListener<T extends IncPacket>
	{
		Class<T> clazz;
		Callback<T> callback;
		int transactionID;
	}
}
