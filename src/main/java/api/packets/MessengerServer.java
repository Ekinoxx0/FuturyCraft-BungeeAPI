package api.packets;

import api.Main;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 17/12/2016.
 */
public class MessengerServer
{
	private final ServerSocket server;
	private final int port;
	private final String[] whitelist;
	private final Lock listLock = new ReentrantLock();
	private final Map<ServerInfo, MessengerClient> connected = new HashMap<>();
	private final List<MessengerClient> nonRegistered = new ArrayList<>(); //Else, non-registered Clients might be
	// garbage collected
	private Thread connectionListener;
	private transient boolean end = false;

	public MessengerServer(int port, String[] whitelist)
	{
		this.port = port;
		this.whitelist = whitelist;

		try
		{
			server = new ServerSocket(port);
			setupConnectionListener();
		}
		catch (Exception e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while creating the ServerSocket (Server: " + this
					+ ")", e);

			//ProxyServer.getInstance().stop(); //Stop because this server is not usable now

			throw new RuntimeException(e);
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

					MessengerClient client = new MessengerClient(socket, this); // Will register itself once identified

					listLock.lock();
					nonRegistered.add(client);
					listLock.unlock();
				}
				catch (IOException e)
				{
					if (!end)
						Main.getInstance().getLogger().log(Level.SEVERE, "Error while accepting a new socket " +
										"connection (Server: " + this + ")",
								e);
				}
				catch (Exception e)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "Error while accepting a new socket " +
									"connection (Server: " + this + ")",
							e);
				}
			}
		}
		);

		connectionListener.start();
	}

	void register(ServerInfo info, MessengerClient client) //Called by the client listener once identified
	{
		listLock.lock();
		nonRegistered.remove(client);
		connected.put(info, client);
		listLock.unlock();
	}

	void unregister(MessengerClient client)
	{
		listLock.lock();
		nonRegistered.remove(client); //Remove if present
		connected.remove(client.getInfo()); //Remove if present
		listLock.unlock();
	}

	@Override
	public String toString()
	{
		return "MessengerServer{" +
				"server=" + String.valueOf(server) +
				", port=" + port +
				", whitelist=" + Arrays.toString(whitelist) +
				", listLock=" + listLock +
				", connected=" + connected +
				", nonRegistered=" + nonRegistered +
				", connectionListener=" + connectionListener +
				", end=" + end +
				'}';
	}
}
