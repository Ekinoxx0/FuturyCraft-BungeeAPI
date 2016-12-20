package api.packets;

import api.Main;
import api.data.Server;
import net.md_5.bungee.api.ProxyServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;
/**
 * Created by SkyBeast on 17/12/2016.
 */
public class MessengerServer
{
	private ServerSocket server;
	private final int port;
	private final String[] whiteList;
	private final List<MessengerClient> nonRegistered = new ArrayList<>();
	private Thread connectionListener;
	private volatile boolean end = false;
	private boolean init = false;
	public MessengerServer(int port, String[] whiteList)
	{
		this.port = port;
		this.whiteList = whiteList;
	}
	public void init()
	{
		if (!init)
		{
			try
			{
				server = new ServerSocket(port);
				setupConnectionListener();
			}
			catch (Exception e)
			{
				Main.getInstance().getLogger().log(Level.SEVERE, "Error while creating the ServerSocket (Server: " +
						this
						+ ")", e);
				ProxyServer.getInstance().stop();
				throw new IllegalStateException(e);
			}
		}
	}
	private void setupConnectionListener()
	{
		connectionListener = new Thread(() ->
		{
			while (!server.isClosed())
			{
				try
				{
					Socket socket = server.accept();
					if (Stream.of(whiteList)
							.anyMatch(entry -> socket.getInetAddress().getHostName().equals(entry) ||
									socket.getInetAddress().getHostAddress().equals(entry)))
					{
						Main.getInstance().getLogger().log(Level.WARNING, "Socket did not pass the white-list: " +
								socket);
						socket.getOutputStream().write(0); //Write false
						socket.close();
						continue;
					}
					MessengerClient client = new MessengerClient(socket, this);
					synchronized (nonRegistered)
					{
						nonRegistered.add(client);
					}
				}
				catch (IOException e)
				{
					if (!end)
						Main.getInstance().getLogger().log(Level.SEVERE, "Error while accepting new sockets " +
										"connection (Server: " + this + ")",
								e);
				}
				catch (Exception e)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "Error while accepting new sockets " +
									"connection (Server: " + this + ")",
							e);
				}
			}
		}
		);
		connectionListener.start();
	}
	public void stop()
	{
		end = true;
		try
		{
			server.close();
		}
		catch (IOException ignored) {}
		synchronized (nonRegistered)
		{
			nonRegistered.forEach(MessengerClient::disconnect);
			nonRegistered.clear();
		}
	}
	void register(Server server, MessengerClient client) //Called by the client listener once identified
	{
		synchronized (nonRegistered)
		{
			nonRegistered.remove(client);
		}
		Main.getInstance().getDataManager().updateMessenger(server, client);
	}
	void unregister(MessengerClient client)
	{
		synchronized (nonRegistered)
		{
			nonRegistered.remove(client); //Remove if present
		}
	}
	@Override
	public String toString()
	{
		return "MessengerServer{" +
				"server=" + String.valueOf(server) +
				", port=" + port +
				", whiteList=" + Arrays.toString(whiteList) +
				", nonRegistered=" + nonRegistered +
				", connectionListener=" + connectionListener +
				", end=" + end +
				", init=" + init +
				'}';
	}
}
