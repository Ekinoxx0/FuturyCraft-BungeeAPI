package api.packets;

import api.Main;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
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
	private Thread listener;
	private ServerInfo server;
	private transient boolean end = false;


	MessengerClient(Socket socket, MessengerServer messengerServer) throws IOException //Called in Server connection
	// listener
	{
		this.messengerServer = messengerServer;
		this.socket = socket;
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		setupSocketListener();
	}

	private void setupSocketListener() //Called in Server connection listener
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
			}
			catch (Exception e)
			{
				Main.getInstance().getLogger().log(Level.SEVERE, "Error while reading the identifier (Client: " + this
								+ ")",
						e);
			}

			//Socket identified

			while (!socket.isClosed())
			{
				try
				{
					byte cmd = in.readByte();
					short i = in.readShort();
					byte[] data = new byte[i];

					in.readFully(data); //Read all data and store it to the array

					handleData(data);
				}
				catch (Exception e)
				{
					if (!end)
						Main.getInstance().getLogger().log(Level.SEVERE, "Error while reading a command (Client: " +
										this + ")",
								e);
				}
			}
		}
		);

		listener.start();
	}

	private void handleData(byte[] arrayIn) throws IOException
	{
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(arrayIn)); //Create an InputStream
		// from the byte array, so it can be redistributed

		byte id = data.readByte();

		try
		{

			Packet packet = Packets.constructPacket(id, data);
		}
		catch (ReflectiveOperationException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while constructing packet id " + id + " with " +
					"data" +
					" " + Arrays.toString(arrayIn) + " (Client: " + this + ")", e);
		}
	}

	private boolean identify(int port)
	{
		for (ServerInfo info : ProxyServer.getInstance().getServers().values())
		{
			if (info.getAddress().getPort() == port && Arrays.equals(info.getAddress().getAddress().getAddress(),
					socket.getInetAddress().getAddress()))
			{
				this.server = info;
				messengerServer.register(info, this);
				return true;
			}
		}

		return false;
	}

	public void sendPacket(OutPacket packet) throws IOException
	{
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(new ByteArrayOutputStream());
		packet.write(data);

		synchronized (out)
		{
			out.write(Packets.getID(packet.getClass()));
			out.write(array.size());
			out.write(array.toByteArray());
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

	public ServerInfo getInfo()
	{
		return server;
	}

	@Override
	public String toString()
	{
		return "MessengerClient{" +
				"messengerServer=" + messengerServer +
				", socket=" + socket +
				", in=" + in +
				", out=" + out +
				", listener=" + listener +
				", server=" + server +
				", end=" + end +
				'}';
	}
}
