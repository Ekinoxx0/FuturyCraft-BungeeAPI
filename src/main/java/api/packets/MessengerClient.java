package api.packets;

import api.Main;
import api.data.Server;
import api.events.PacketReceivedEvent;
import api.utils.concurrent.Callback;
import api.utils.concurrent.ThreadLoop;
import api.utils.concurrent.ThreadLoops;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 17/12/2016.
 */
@ToString(exclude = "server")
public class MessengerClient
{
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private final List<PacketListener<?>> listeners = new CopyOnWriteArrayList<>();
	private final ExecutorService sendPacketPool = Executors.newSingleThreadExecutor();
	private final BlockingQueue<PacketData> sendBuffer = new ArrayBlockingQueue<>(20);
	@Getter(AccessLevel.PACKAGE)
	protected Server server;
	private final AtomicInteger lastTransactionID = new AtomicInteger();
	private final ThreadLoop listener = setupListenerThreadLoop();
	private final ThreadLoop sender = setupListenerThreadLoop();
	private volatile boolean end;

	public MessengerClient(Socket socket, DataInputStream in, DataOutputStream out, Server server)
			throws IOException //Called in MessengerClient identifier thread
	{
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.server = server;
		listener.start();
	}

	private ThreadLoop setupListenerThreadLoop() //Called in MessengerClient connection listener
	{
		return ThreadLoops.newConditionThreadLoop
				(
						() -> !socket.isClosed() && !end,
						() ->
						{
							try
							{
								byte id = in.readByte();
								int transactionID = in.readShort();
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

	private ThreadLoop setupSenderThreadLoop()
	{
		return ThreadLoops.newInfiniteThreadLoop(
				() ->
				{
					try
					{
						PacketData toSend = sendBuffer.take();
						out.writeByte(toSend.packetID);
						out.writeInt(toSend.transactionID);
						out.writeShort(toSend.data.length);
						out.write(toSend.data);
						out.flush();
					}
					catch (IOException e)
					{
						Main.getInstance().getLogger().log(Level.SEVERE, "Cannot send buffered packet!", e);
					}
				}
		);
	}

	@SuppressWarnings("unchecked")
	protected void handleData(byte id, int transactionID, byte[] arrayIn) throws IOException
	{
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(arrayIn)); //Create an InputStream
		// from the byte array, so it can be redistributed

		try
		{
			IncPacket packet = Packets.constructIncomingPacket(id, data);

			if (packet == null)
				throw new IllegalArgumentException("Cannot find packet ID " + id + " (transactionID=" + transactionID
						+ ", in=" + Arrays.toString(arrayIn) + ')');

			for (Iterator<PacketListener<?>> iterator = listeners.iterator(); iterator.hasNext(); )
			{
				PacketListener<IncPacket> listener = (PacketListener<IncPacket>) iterator.next();
				if (listener.transactionID != transactionID || listener.clazz != packet.getClass())
					continue;

				listener.callback.response(packet);
				iterator.remove();
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
		listeners.add(new PacketListener<>(clazz, callback, transactionID));
	}

	public int sendPacket(OutPacket packet)
	{
		int transactionID = lastTransactionID.getAndIncrement();
		sendPacket(packet, transactionID);
		return transactionID;
	}

	public void sendPacket(OutPacket packet, int transactionID)
	{
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(array);
		try
		{
			packet.write(data);
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Cannot serialize packet " + packet, e);
		}

		boolean bool = false;
		try
		{
			bool = sendBuffer.offer(new PacketData(getPacketID(packet), transactionID,
							array.toByteArray()),
					10000,
					TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			cannotBuffer(e);
		}

		if (!bool)
			cannotBuffer(null);
	}

	private void cannotBuffer(InterruptedException e)
	{
		Main.getInstance().getLogger().log(Level.SEVERE, "Cannot buffer packet!");
		if (e != null)
			Main.getInstance().getLogger().log(Level.SEVERE, "Exception:", e);
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

	@ToString
	@AllArgsConstructor
	private static class PacketData
	{
		byte packetID;
		int transactionID;
		byte[] data;
	}
}
