package api.utils;

import api.Main;
import api.data.Server;
import api.data.UserData;
import api.events.PlayerConnectToServerEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerConnectEvent;
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

		event.setTarget(ProxyServer.getInstance().getPluginManager().callEvent(
				new PlayerConnectToServerEvent(
						from == null ? null : Server.get(from.getInfo()),
						UserData.get(event.getPlayer()),
						from == null ? PlayerConnectToServerEvent.ConnectionCause.NETWORK_CONNECT :
								PlayerConnectToServerEvent.ConnectionCause.SERVER_SWITCH,
						Server.get(event.getTarget())
				)
		).getTo().getInfo());
	}
}
