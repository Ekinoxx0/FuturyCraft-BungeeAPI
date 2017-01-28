package api.lobby;

import api.Main;
import api.config.Template;
import api.config.Variant;
import api.data.Server;
import api.deployer.Deployer;
import api.deployer.DeployerServer;
import api.deployer.Lobby;
import api.events.PlayerConnectToServerEvent;
import api.events.PlayerDisconnectFromServerEvent;
import api.packets.server.StopPacket;
import api.utils.SimpleManager;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by SkyBeast on 27/01/17.
 */
@ToString
public final class LobbyManager implements SimpleManager
{
	private static final int ACCEPT_PLAYERS = 50;
	private static final int WARNING_TIME = 1000 * 60 * 13; //13min
	private static final int SERVER_STOP_TIME = 1000 * 60 * 15; //15min
	private static final BaseComponent[] WARNING = new ComponentBuilder("Vous avez été déconnecté car le serveur " +
			"va redémarrer.").color(ChatColor.GREEN).create();
	private static final BaseComponent[] SERVER_STOP = new ComponentBuilder("Le serveur va redémarrer dans 2 " +
			"minutes.").color(ChatColor.GREEN).create();
	private final Listen listener = new Listen();
	private boolean init;
	private volatile boolean end;
	private Server acceptLobby;
	private Server waitingLobby;
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialized!");

		ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), listener);

		acceptLobby = deployLobby();
		waitingLobby = deployLobby();

		init = true;
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");

		end = true;

		Main.getInstance().getLogger().info(this + " stopped.");
	}

	private void changeAcceptLobby()
	{
		((Lobby) acceptLobby.getDeployer()).setStopAcceptPlayerTimestamp(System.currentTimeMillis());

		acceptLobby = waitingLobby;
		waitingLobby = deployLobby();
	}

	private void scheduleWarn(Server server)
	{
		executorService.schedule
				(
						() ->
						{
							if (!server.isStarted())
								return;

							server.getInfo().getPlayers().forEach
									(
											player -> player.sendMessage(WARNING)
									);
							scheduleStop(server);
						},
						WARNING_TIME,
						TimeUnit.MILLISECONDS
				);
	}

	private void scheduleStop(Server server)
	{
		executorService.schedule
				(
						() ->
						{
							if (!server.isStarted())
								return;

							server.getInfo().getPlayers().forEach
									(
											player ->
											{
												player.sendMessage(SERVER_STOP);
												player.connect(acceptLobby.getInfo());
											}
									);

							undeployLobby(server);
						},
						SERVER_STOP_TIME,
						TimeUnit.MILLISECONDS
				);
	}

	private Server deployLobby()
	{
		Deployer deployer = Main.getInstance().getDeployer();
		int id = deployer.getNextId();
		int port = deployer.getNextPort();
		Variant v = getNextLobbyVariant(Lobby.LobbyType.NORMAL);
		DeployerServer server = new Lobby(id, Lobby.LobbyType.NORMAL, v, port);
		return new Server(server, server.deploy());
	}

	private Variant getNextLobbyVariant(Lobby.LobbyType type)
	{
		List<Template.LobbyTemplate> lobbies = Main.getInstance().getDeployer().getConfig().getLobbiesByType(type);
		if (lobbies.isEmpty())
			return null;
		Template.LobbyTemplate t = lobbies.stream().findFirst().orElse(null);
		if (t == null)
			return null;
		if (lobbies.size() == 1)
			return t.getVariants().get(0);
		t.setOffset(t.getOffset() + 1);
		if (t.getOffset() >= t.getVariants().size())
			t.setOffset(0);
		return t.getVariants().get(t.getOffset());
	}

	private void undeployLobby(Server server)
	{
		server.getDeployer().kill();
	}

	public class Listen implements Listener
	{
		private Listen() {}

		@EventHandler
		public void onPlayerJoinLobby(PlayerConnectToServerEvent event)
		{
			System.out.println("connectToServerEvent = " + event);

			if (event.getCause() == PlayerConnectToServerEvent.ConnectionCause.NETWORK_CONNECT)
				event.setTo(acceptLobby);
			else if (!event.getTo().isLobby() && event.getTo() != acceptLobby)
				return;

			Lobby lobby = (Lobby) event.getTo().getDeployer();
			lobby.incrementAcceptedPlayers();

			if (lobby.getAcceptedPlayers() >= ACCEPT_PLAYERS)
				changeAcceptLobby();
		}

		@EventHandler
		public void onPlayerQuitLobby(PlayerDisconnectFromServerEvent event)
		{
			System.out.println("disconnectToServerEvent = " + event);

			if (!event.getTo().isLobby())
				return;

			Server to = event.getTo();
			Lobby lobby = (Lobby) to.getDeployer();

			if (to == acceptLobby || to == waitingLobby)
				return;

			if (lobby.getAcceptedPlayers() == 0)
			{
				undeployLobby(event.getFrom());
				return;
			}

			if (lobby.getAcceptedPlayers() / lobby.getVariant().getSlots() >= 0.75)
				scheduleWarn(to);
		}
	}
}
