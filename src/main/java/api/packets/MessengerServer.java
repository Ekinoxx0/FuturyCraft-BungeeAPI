package api.packets;

import api.Main;
import api.data.Server;
import api.panel.MessengerPanel;
import api.utils.SimpleManager;
import net.md_5.bungee.api.ProxyServer;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
public class MessengerServer implements SimpleManager
{
	private final int port;
	private final String[] whiteList;
	private final List<Socket> nonRegistered = new ArrayList<>();
	private ServerSocket server;
	private Thread connectionListener;
	private volatile boolean end = false;
	private boolean init = false;

	public MessengerServer(int port, String... whiteList)
	{
		this.port = port;
		this.whiteList = whiteList;
	}

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialized!");

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

	private void setupConnectionListener()
	{
		connectionListener = new Thread(() ->
		{
			while (!server.isClosed())
			{
				try
				{
					Socket socket = server.accept();
					Main.getInstance().getLogger().info("Socket accepted: " +
							socket);

					if (!Stream.of(whiteList)
							.anyMatch(entry -> socket.getInetAddress().getHostName().equals(entry) ||
									socket.getInetAddress().getHostAddress().equals(entry)))
					{
						Main.getInstance().getLogger().log(Level.WARNING, "Socket did not pass the white-list: " +
								socket);
						socket.getOutputStream().write(0); //Write false
						socket.close();
						continue;
					}

					synchronized (nonRegistered)
					{
						nonRegistered.add(socket);
					}

					identify(socket);
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

	private void identify(Socket socket)
	{
		Thread identifier = new Thread(() ->
		{
			try
			{
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

				int port = in.readInt(); //Identify

				if (port == -1 && Main.getInstance().getPanelManager().getMessengerPanel() != null)
				{ //Panel
					new MessengerPanel(socket, in, out); //Will auto-register itself
				}

				Server server = Main.getInstance().getDataManager().findServerByPort(port);

				if (server == null)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "The socket sent a non-registered port for " +
							"identification! Disconnecting it... (Port: " + port + ", Socket: " + socket + ")");

					try {socket.close();}
					catch (IOException ignored) {} //Disconnect
					return; //Don't go further
				}

				out.writeBoolean(true);
				out.flush();

				MessengerClient client = new MessengerClient(socket, in, out, server);

				synchronized (nonRegistered)
				{
					nonRegistered.remove(socket);
				}

				Main.getInstance().getDataManager().updateMessenger(server, client);

				Main.getInstance().getLogger().info(client + " identified");
			}
			catch (Exception e)
			{
				Main.getInstance().getLogger().log(Level.SEVERE, "Error while reading the identifier (Socket: " +
								socket
								+ ")",
						e);
			}
		}
		);

		identifier.start();
	}

	private Server identifyServer(int port)
	{
		Server server = Main.getInstance().getDataManager().findServerByPort(port);
		if (server == null)
			return null;

		return server;
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");

		synchronized (nonRegistered)
		{
			nonRegistered.forEach((socket) ->
			{
				try {socket.close();}
				catch (IOException ignored) {}
			});
			nonRegistered.clear();
		}

		end = true;
		try
		{
			server.close();
		}
		catch (IOException ignored) {}

		Main.getInstance().getDataManager().forEachServers(srv ->
				{
					if (srv.getMessenger() != null)
						srv.getMessenger().disconnect();
				}
		);

		Main.getInstance().getLogger().info(this + " stopped.");
	}

	void unregister(MessengerClient client)
	{
		Main.getInstance().getDataManager().updateMessenger(client.getServer(), null);
	}

	@Override
	public String toString()
	{
		return "MessengerServer{" +
				"server=" + server +
				", port=" + port +
				", whiteList=" + Arrays.toString(whiteList) +
				", nonRegistered=" + nonRegistered +
				", connectionListener=" + connectionListener +
				", end=" + end +
				", init=" + init +
				'}';
	}
}
