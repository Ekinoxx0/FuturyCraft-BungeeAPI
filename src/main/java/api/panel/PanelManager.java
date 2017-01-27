package api.panel;

import api.Main;
import api.data.Server;
import api.events.*;
import api.packets.IncPacket;
import api.panel.packets.*;
import api.utils.SimpleManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SkyBeast on 04/01/2017.
 */
public class PanelManager implements SimpleManager
{
	private final Listen listener = new Listen();
	private MessengerPanel messengerPanel;
	private boolean init;

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialized!");

		Main.getInstance().getProxy().getPluginManager().registerListener(Main.getInstance(), listener);
		init = true;
	}

	public MessengerPanel getMessengerPanel()
	{
		return messengerPanel;
	}

	void setMessengerPanel(MessengerPanel messengerPanel)
	{
		this.messengerPanel = messengerPanel;
	}

	public Listen getListener()
	{
		return listener;
	}

	public class Listen implements Listener
	{
		private Listen() {}

		@EventHandler
		public void onPanelPacket(PanelPacketReceivedEvent event)
		{
			IncPacket packet = event.getPacket();

			if (packet instanceof InHeaderPanelPacket)
			{
				listenHeader = ((InHeaderPanelPacket) packet).isListen();
				sendHeader();
			}
			else if (packet instanceof InServerListPanelPacket)
			{
				listenServerList = ((InServerListPanelPacket) packet).isListen();
				sendServerList();
			}
			else if (packet instanceof InServerInfoPanelPacket)
			{
				InServerInfoPanelPacket packet1 = (InServerInfoPanelPacket) packet;
				Server server = Main.getInstance().getDataManager().getServer(packet1.getUuid());
				if (server == null)
				{
					messengerPanel.sendPacket(new IllegalServerInfoPacket(packet1.getUuid()));
					return;
				}
				else if (packet1.isListen())
				{
					if (!listenServerInfo.contains(server)) {listenServerInfo.add(server);}
				}
				else
				{
					listenServerInfo.remove(server); //Remove if present
				}

				sendServerInfo(server, true);
			}
			else if (packet instanceof ConsoleInputServerInfoPanelPacket)
			{
				ConsoleInputServerInfoPanelPacket packet1 = (ConsoleInputServerInfoPanelPacket) packet;
				Server server = Main.getInstance().getDataManager().getServer(packet1.getServerUUID());
				if (server == null)
				{
					Main.getInstance().getLogger().severe("Panel sent command to unknown server.");
					return;
				}

				server.getDeployer().sendCommand(packet1.getIn());
			}
		}

		//@formatter:off
		boolean listenHeader;
		@EventHandler public void pHeader(ServerDeployedEvent   e) {sendHeader();}
		@EventHandler public void pHeader(ServerUndeployedEvent e) {sendHeader();}
		@EventHandler public void pHeader(LoginEvent            e) {sendHeader();}
		@EventHandler public void pHeader(PlayerDisconnectEvent e) {sendHeader();}
		//@formatter:on

		void sendHeader()
		{
			if (!listenHeader || messengerPanel == null) return;
			messengerPanel.sendPacket(new OutHeaderPanelPacket((short) ProxyServer.getInstance().getOnlineCount(),
					(short) Main.getInstance().getDeployer().getMaxPlayers(),
					(short) Main.getInstance().getDataManager().getServerCount()));
		}

		//@formatter:off
		boolean listenServerList;
		@EventHandler public void pServerList(ServerDeployedEvent             e) {addServerList(e.getServer());}
		@EventHandler public void pServerList(ServerUndeployedEvent           e) {removeServerList(e.getServer());}
		@EventHandler public void pServerList(PlayerConnectToServerEvent      e) {updateServerList(e.getServer());}
		@EventHandler public void pServerList(PlayerDisconnectFromServerEvent e) {updateServerList(e.getServer());}
		@EventHandler public void pServerList(ServerChangeStateEvent          e) {updateServerList(e.getServer());}
		//@formatter:on

		void sendServerList()
		{
			if (!listenServerList || messengerPanel == null) return;

			List<OutServerListPanelPacket.ServerData> servers = new ArrayList<>();
			Main.getInstance().getDataManager().forEachServers(server -> servers.add(OutServerListPanelPacket
					.ServerData.from(server)));
			messengerPanel.sendPacket(new OutServerListPanelPacket(servers));
		}

		void addServerList(Server server)
		{
			if (!listenServerList || messengerPanel == null) return;
			messengerPanel.sendPacket(AddServerListPanelPacket.from(server));
		}

		void removeServerList(Server server)
		{
			if (!listenServerList || messengerPanel == null) return;
			messengerPanel.sendPacket(new RemoveServerListPanelPacket(server.getUuid()));
		}

		void updateServerList(Server server)
		{
			if (!listenServerList || messengerPanel == null) return;
			messengerPanel.sendPacket(UpdateServerListPanelPacket.from(server));
		}

		//@formatter:off
		List<Server> listenServerInfo = new ArrayList<>();
		@EventHandler public void pServerInfo(ServerDeployedEvent             e) {sendServerInfo(e.getServer(), false);}
		@EventHandler public void pServerInfo(ServerUndeployedEvent           e) {sendServerInfo(e.getServer(), false);}
		@EventHandler public void pServerInfo(PlayerConnectToServerEvent      e) {sendServerInfo(e.getServer(), false);}
		@EventHandler public void pServerInfo(PlayerDisconnectFromServerEvent e) {sendServerInfo(e.getServer(), false);}
		@EventHandler public void pServerInfo(ServerChangeStateEvent          e) {sendServerInfo(e.getServer(), false);}
		@EventHandler public void pServerInfo(NewConsoleLineEvent             e) {newLineServerInfo(e.getServer(), e.getLine());}
		//@formatter:on

		void sendServerInfo(Server server, boolean sendConsole)
		{
			if (!listenServerInfo.contains(server) || messengerPanel == null) return;
			messengerPanel.sendPacket(OutServerInfoPanelPacket.from(server, sendConsole));
		}

		void newLineServerInfo(Server server, String line)
		{
			if (!listenServerInfo.contains(server) || messengerPanel == null) return;
			messengerPanel.sendPacket(new ConsoleOutputServerInfoPanelPacket(server.getUuid(), line));
		}

		public void resetListening()
		{
			listenHeader = false;
			listenServerList = false;
			listenServerInfo.clear();
		}
	}
}
