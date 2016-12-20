package api.packets;

import api.Main;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Created by SkyBeast on 17/12/2016.
 */
public class MessengerServer
{
	private final ServerSocket server;
	private final int port;
	private final String[] whiteList;
	private final Lock listLock = new ReentrantLock();
	private final Map<ServerInfo, MessengerClient> connected = new HashMap<>();
	private final List<MessengerClient> nonRegistered = new ArrayList<>(); //Else, non-registered Clients might be
	// garbage collected
	private Thread connectionListener;
	private volatile boolean end = false;

	public MessengerServer(int port, String[] whiteList)
	{
		this.port = port;
		this.whiteList = whiteList;

		try
		{
			server = new ServerSocket(port);
			setupConnectionListener();
		}
		catch (Exception e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while creating the ServerSocket (DeployerServer: " + this
					+ ")", e);

			ProxyServer.getInstance().stop(); //Stop because this server is not usable now

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

					MessengerClient client = new MessengerClient(socket, this); //Will register itself once identified

					listLock.lock();
					try
					{
						nonRegistered.add(client);
					}
					finally
					{
						listLock.unlock();
					}
				}
				catch (IOException e)
				{
					if (!end)
						Main.getInstance().getLogger().log(Level.SEVERE, "Error while accepting new sockets " +
										"connection (DeployerServer: " + this + ")",
								e);
				}
				catch (Exception e)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "Error while accepting new sockets " +
									"connection (DeployerServer: " + this + ")",
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

		listLock.lock();
		try
		{
			nonRegistered.forEach(MessengerClient::disconnect);
			nonRegistered.clear();
			connected.values().forEach(MessengerClient::disconnect);
			connected.clear();
		}
		finally
		{
			listLock.unlock();
		}

	}

	void register(ServerInfo info, MessengerClient client) //Called by the client listener once identified
	{
		listLock.lock();
		try
		{
			nonRegistered.remove(client);
			connected.put(info, client);
		}
		finally
		{
			listLock.unlock();
		}
	}

	void unregister(MessengerClient client)
	{
		listLock.lock();
		try
		{
			nonRegistered.remove(client); //Remove if present
			connected.remove(client.getInfo()); //Remove if present
			listLock.unlock();
		}
		finally
		{
			listLock.unlock();
		}
	}

	@Override
	public String toString()
	{
		return "MessengerServer{" +
				"server=" + String.valueOf(server) +
				", port=" + port +
				", whiteList=" + Arrays.toString(whiteList) +
				", listLock=" + listLock +
				", connected=" + connected +
				", nonRegistered=" + nonRegistered +
				", connectionListener=" + connectionListener +
				", end=" + end +
				'}';
	}
}
