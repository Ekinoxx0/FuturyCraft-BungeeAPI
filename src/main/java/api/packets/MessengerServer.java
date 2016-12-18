package api.packets;

import api.Main;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by SkyBeast on 17/12/2016.
 */
public class MessengerServer {
	private final ServerSocket server;
	private final int port;
	private Thread connectionListener;
	private final Map<ServerInfo, MessengerClient> connected = new HashMap<>();
	private final List<MessengerClient> nonRegistered = new ArrayList<>(); //Else, non-registered Clients might be
	// garbage collected

	public MessengerServer(int port)
	{
		this.port = port;

		try
		{
			server = new ServerSocket(port);
			setupConnectionListener();
		}
		catch (Exception e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while creating the ServerSocket", e);

			//ProxyServer.getInstance().stop(); //Stop because this server is not usable now

			throw new RuntimeException(e);
		}

	}

	private void setupConnectionListener()
	{
		connectionListener = new Thread(() ->
		{
			while (true)
			{
				try
				{
					Socket socket = server.accept();

					nonRegistered.add(new MessengerClient(socket, this)); // Will register itself once identified
				}
				catch (Exception e)
				{
					Main.getInstance().getLogger().log(Level.SEVERE, "Error while accepting a new socket connection",
							e);
				}
			}
		}
		);

		connectionListener.start();
	}

	void register(ServerInfo info, MessengerClient client) //Called by the client listener once identified
	{
		nonRegistered.remove(client);
		connected.put(info, client);
	}

	void unregister(MessengerClient client)
	{
		nonRegistered.remove(client); //Remove if present
		connected.remove(client.getInfo()); //Remove if present
	}
}
