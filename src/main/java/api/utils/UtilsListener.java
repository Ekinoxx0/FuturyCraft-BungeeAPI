package api.utils;

import api.Main;
import api.commands.BossBarMessageCommand;
import api.data.DataManager;
import api.data.Server;
import api.data.UserData;
import api.events.PacketReceivedEvent;
import api.events.PlayerConnectToServerEvent;
import api.events.PlayerDisconnectFromServerEvent;
import api.packets.server.BossBarMessagesPacket;
import api.packets.server.InBossBarMessages;
import com.mongodb.client.FindIterable;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SkyBeast on 27/01/17.
 */
public class UtilsListener implements SimpleManager, Listener
{

	public static final List<BossBarMessagesPacket.MessageData> BOSS_BAR_MESSAGES = new ArrayList<>();

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
						get(event.getPlayer()),
						PlayerConnectToServerEvent.ConnectionCause.NETWORK_CONNECT,
						Server.get(event.getTarget())
				)
				: new PlayerConnectToServerEvent
				(
						Server.get(from.getInfo()),
						get(event.getPlayer()),
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
									get(event.getPlayer()),
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
										get(event.getPlayer()),
										PlayerDisconnectFromServerEvent.ConnectionCause.NETWORK_DISCONNECT,
										null
								)
				);
	}

	public UserData get(ProxiedPlayer p)
	{
		return Main.getInstance().getUserDataManager().getData(p);
	}

	@EventHandler
	public void onReceivePacket(PacketReceivedEvent e)
	{
		if(e.getPacket() instanceof InBossBarMessages)
		{

			if (BOSS_BAR_MESSAGES.isEmpty())
			{
				FindIterable<Document> fi = BossBarMessageCommand.getCOLLECTION().find();
				for (Document doc : fi)
					BOSS_BAR_MESSAGES.add(new BossBarMessagesPacket.MessageData(String.valueOf(doc.get("message")), (int) doc.get("time")));
			}

			sendBossBarMessagesPacket(e.getFrom());
		}
	}

	public void sendBossBarMessagesPacket(Server s)
	{
		s.getMessenger().sendPacket(new BossBarMessagesPacket(BOSS_BAR_MESSAGES));
	}

	private <T extends Event> T callEvent(T event)
	{
		return ProxyServer.getInstance().getPluginManager().callEvent(event);
	}
}
