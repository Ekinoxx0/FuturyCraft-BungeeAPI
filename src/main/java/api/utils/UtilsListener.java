package api.utils;

import api.Main;
import api.commands.BossBarMessageCommand;
import api.data.Server;
import api.data.UserData;
import api.events.PacketReceivedEvent;
import api.events.PlayerConnectToServerEvent;
import api.events.PlayerDisconnectFromServerEvent;
import api.packets.server.BossBarMessagesPacket;
import api.packets.server.RequestBossBarMessagesPacket;
import com.mongodb.client.FindIterable;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SkyBeast on 27/01/17.
 */
public final class UtilsListener implements SimpleManager, Listener
{
	private final List<BossBarMessagesPacket.MessageData> bossBarMessages = new ArrayList<>();

	@Override
	public void init()
	{
		Main.registerListener(this);
	}

	public static UtilsListener instance()
	{
		return Main.getInstance().getUtilsListener();
	}

	public void addBossBarMessage(BossBarMessagesPacket.MessageData data)
	{
		bossBarMessages.add(data);
	}

	public void removeBossBarMessage(int index)
	{
		bossBarMessages.remove(index);
	}

	@EventHandler
	public void onConnect(ServerConnectEvent event)
	{
		net.md_5.bungee.api.connection.Server from = event.getPlayer().getServer();

		UserData data = UserData.get(event.getPlayer());

		PlayerConnectToServerEvent newEvent = from == null
				? new PlayerConnectToServerEvent
				(
						null,
						data,
						PlayerConnectToServerEvent.ConnectionCause.NETWORK_CONNECT,
						Server.get(event.getTarget())
				)
				: new PlayerConnectToServerEvent
				(
						Server.get(from.getInfo()),
						data,
						PlayerConnectToServerEvent.ConnectionCause.SERVER_SWITCH,
						Server.get(event.getTarget())
				);
		Main.callEvent(newEvent);

		Server to = newEvent.getTo();
		if (to != null)
			event.setTarget(to.getInfo());

		if (from != null)
		{
			Main.callEvent(
					new PlayerDisconnectFromServerEvent
							(
									Server.get(from.getInfo()),
									data,
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

		UserData data = UserData.get(event.getPlayer());

		Main.callEvent
				(
						new PlayerDisconnectFromServerEvent
								(
										server == null ? null : Server.get(server.getInfo()),
										data,
										PlayerDisconnectFromServerEvent.ConnectionCause.NETWORK_DISCONNECT,
										null
								)
				);
	}

	@EventHandler
	public void onReceivePacket(PacketReceivedEvent e)
	{
		if (e.getPacket() instanceof RequestBossBarMessagesPacket)
		{

			if (bossBarMessages.isEmpty())
			{
				FindIterable<Document> fi = BossBarMessageCommand.getCOLLECTION().find();
				for (Document doc : fi)
					bossBarMessages.add(new BossBarMessagesPacket.MessageData(String.valueOf(doc.get("message")),
							(int) doc.get("time")));
			}

			sendBossBarMessagesPacket(e.getFrom());
		}
	}

	public void sendBossBarMessagesPacket(Server server)
	{
		server.sendPacket(new BossBarMessagesPacket(bossBarMessages));
	}
}
