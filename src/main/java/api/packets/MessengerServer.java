package api.packets;

import api.Main;
import api.data.Server;
import api.panel.MessengerPanel;
import api.utils.SimpleManager;
import api.utils.concurrent.ThreadLoop;
import api.utils.concurrent.ThreadLoops;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Created by SkyBeast on 17/12/2016.
 */
@ToString
public final class MessengerServer implements SimpleManager
{
	private static final int PORT = 5555;
	private static final String[] WHITELIST = {"localhost", "127.0.0.1"};
	private final List<Socket> nonRegistered = new CopyOnWriteArrayList<>(); //Keep an instance
	private ServerSocket server;
	private final ThreadLoop connectionListener = setupConnectionListener();
	private volatile boolean end;
	private boolean init;

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialized!");

		try
		{
			server = new ServerSocket(PORT);
			connectionListener.start();
		}
		catch (Exception e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while creating the ServerSocket (Server: " +
					this + ')', e);
			ProxyServer.getInstance().stop();
			throw new IllegalStateException(e);
		}
	}

	/*
	 * Handle any new connection.
	 */
	private ThreadLoop setupConnectionListener()
	{
		return ThreadLoops.newConditionThreadLoop
				(
						() -> !server.isClosed(),
						() ->
						{
							try
							{
								Socket socket = server.accept();
								Main.getInstance().getLogger().info("Socket accepted: " +
										socket);

								handleConnection(socket);
							}
							catch (IOException e)
							{
								if (!end)
									Main.getInstance().getLogger().log(Level.SEVERE, "Error while accepting new " +
													"sockets connection (Server: " + this + ')',
											e);
							}
						}

				);
	}

	/**
	 * Handle a new connection.
	 *
	 * @param socket the connection
	 */
	private void handleConnection(Socket socket)
	{
		if (passWhitelist(socket))
		{
			nonRegistered.add(socket);

			identify(socket);
		}
		else
		{
			Main.getInstance().getLogger().log(Level.WARNING, "Socket did not pass the " +
					"white-list: " + socket);
			closeConnection(socket);
		}
	}

	/**
	 * Do this connection pass the whitelist?
	 *
	 * @param socket the connection
	 * @return whether or not the whitelist is passed
	 */
	private boolean passWhitelist(Socket socket)
	{
		return Stream.of(WHITELIST)
				.anyMatch(entry -> socket.getInetAddress().getHostName().equals(entry) ||
						socket.getInetAddress().getHostAddress().equals(entry));
	}

	/**
	 * Start a identifier thread for this connection.
	 *
	 * @param socket the connection
	 */
	private void identify(Socket socket)
	{
		Thread identifier = new Thread(() ->
		{
			try
			{
				handleIdentify(socket);
			}
			catch (IOException e)
			{
				Main.getInstance().getLogger().log(Level.SEVERE, "Error while reading the identifier (Socket: " +
						socket + ')', e);
			}
		}
		);

		identifier.start();
	}

	/**
	 * Identify the origin of the connection.
	 *
	 * @param socket the connection
	 * @throws IOException uses in/out streams
	 */
	private void handleIdentify(Socket socket) throws IOException
	{
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		int port = in.readInt(); //Identify

		if (isPanel(port)) //Panel
		{
			panel(socket, in, out);
			return;
		}

		Server server = Main.getInstance().getDataManager().findServerByPort(port);
		if (server == null)
		{
			rejected(socket, port); //Socket connection not accepted
			return;
		}

		identified(server, socket, in, out); //Identified!
	}

	/**
	 * Is this connection a panel connection?
	 *
	 * @param port the port sent
	 * @return whether or not the connection is a panel connection
	 */
	private boolean isPanel(int port)
	{
		return port == -1 && Main.getInstance().getPanelManager().getMessengerPanel() == null;
	}

	/**
	 * Treat this connection as a panel.
	 *
	 * @param socket the connection
	 * @param in     the input stream
	 * @param out    the output stream
	 * @throws IOException uses in/out streams
	 */
	private void panel(Socket socket, DataInputStream in, DataOutputStream out) throws IOException
	{
		MessengerPanel client = new MessengerPanel(socket, in, out);
		client.register();
		out.writeBoolean(true);
		out.flush();
	}

	/**
	 * Treat this connection as a rejected connection.
	 *
	 * @param socket the connection
	 * @param port   the port sent
	 */
	private void rejected(Socket socket, int port)
	{
		Main.getInstance().getLogger().log(Level.SEVERE, "The socket sent a non-registered port for " +
				"identification! Disconnecting it... (Port: " + port + ", Socket: " + socket + ')');

		try {socket.close();}
		catch (IOException ignored) {} //Disconnect
	}

	/**
	 * Treat this connection as a connection to a spigot server.
	 *
	 * @param server the Server instance
	 * @param socket the connection
	 * @param in     the input stream
	 * @param out    the output stream
	 * @throws IOException uses in/out streams
	 */
	private void identified(Server server, Socket socket, DataInputStream in, DataOutputStream out) throws IOException
	{
		out.writeBoolean(true);
		out.flush();

		MessengerClient client = new MessengerClient(socket, in, out, server);

		nonRegistered.remove(socket);

		Main.getInstance().getDataManager().updateMessenger(server, client);

		Main.getInstance().getLogger().info(client + " identified");
	}

	/**
	 * Close a connection.
	 *
	 * @param socket the connection
	 */
	private void closeConnection(Socket socket)
	{
		try {socket.close();}
		catch (IOException ignored) {}
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");

		end = true;
		connectionListener.stop();

		closeNonRegistered();

		try
		{
			server.close();
		}
		catch (IOException ignored) {}

		closeMessengers();

		Main.getInstance().getLogger().info(this + " stopped.");
	}

	/**
	 * Close all non-registered connections.
	 */
	private void closeNonRegistered()
	{
		nonRegistered.forEach(this::closeConnection);
		nonRegistered.clear();
	}

	/**
	 * Close all messengers.
	 */
	private void closeMessengers()
	{
		Main.getInstance().getDataManager().forEachServers(srv ->
				{
					if (srv.getMessenger() != null)
						srv.getMessenger().disconnect();
				}
		);
	}

	/**
	 * Unregister a client.
	 *
	 * @param client the client
	 */
	void unregister(MessengerClient client)
	{
		Main.getInstance().getDataManager().updateMessenger(client.getServer(), null);
	}
}
