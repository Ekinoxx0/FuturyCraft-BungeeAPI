package api.utils;

import api.Main;
import api.data.Server;
import api.data.UserData;
import api.events.PlayerConnectToServerEvent;
import api.events.PlayerDisconnectFromServerEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by SkyBeast on 27/01/17.
 */
public class UtilsListener implements SimpleManager, Listener
{
	@Override
	public void init()
	{
		ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@EventHandler
	public void onConnect(ServerConnectEvent event)
	{
		net.md_5.bungee.api.connection.Server from = event.getPlayer().getServer();

		PlayerConnectToServerEvent newEvent = from == null
				? new PlayerConnectToServerEvent
				(
						null,
						UserData.get(event.getPlayer()),
						PlayerConnectToServerEvent.ConnectionCause.NETWORK_CONNECT,
						Server.get(event.getTarget())
				)
				: new PlayerConnectToServerEvent
				(
						Server.get(from.getInfo()),
						UserData.get(event.getPlayer()),
						PlayerConnectToServerEvent.ConnectionCause.SERVER_SWITCH,
						Server.get(event.getTarget())
				);
		callEvent(newEvent);

		Server to = newEvent.getTo();
		if (to != null)
			event.setTarget(to.getInfo());

		if (from != null)
		{
			callEvent(
					new PlayerDisconnectFromServerEvent
							(
									Server.get(from.getInfo()),
									UserData.get(event.getPlayer()),
									PlayerDisconnectFromServerEvent.ConnectionCause.SERVER_SWITCH,
									newEvent.getTo()
							)
			);
		}
	}

	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event)
	{
		net.md_5.bungee.api.connection.Server server = event.getPlayer().getServer();
		callEvent
				(
						new PlayerDisconnectFromServerEvent
								(
										server == null ? null : Server.get(server.getInfo()),
										UserData.get(event.getPlayer()),
										PlayerDisconnectFromServerEvent.ConnectionCause.NETWORK_DISCONNECT,
										null
								)
				);
	}

	private <T extends Event> T callEvent(T event)
	{
		return ProxyServer.getInstance().getPluginManager().callEvent(event);
	}
}
