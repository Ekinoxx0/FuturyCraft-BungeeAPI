package api.lobby;

import api.Main;
import api.config.DeployerConfig;
import api.config.Template;
import api.config.Variant;
import api.data.Server;
import api.data.UserData;
import api.events.DeployerConfigReloadEvent;
import api.events.PlayerConnectToServerEvent;
import api.events.PlayerDisconnectFromServerEvent;
import api.utils.SimpleManager;
import api.utils.concurrent.Callback;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
	private static final int SERVER_STOP_TIME = 1000 * 60 * 2; //2min
	private static final BaseComponent[] WARNING = new ComponentBuilder("Vous avez été déconnecté car le serveur va " +
			"redémarrer.").color(ChatColor.GREEN).create();
	private static final BaseComponent[] SERVER_STOP = new ComponentBuilder("Le serveur va redémarrer dans 2 " +
			"minutes.").color(ChatColor.RED).create();
	private static final BaseComponent[] SERVER_STARTING = new ComponentBuilder("Le serveur démarre").color(ChatColor
			.RED).create();
	private static final BaseComponent[] SERVER_FULL = new ComponentBuilder("Le serveur est plein ! Réessayez plus " +
			"tard.").color(ChatColor.RED).create();

	private final Listen listener = new Listen();
	private final TObjectIntMap<Server> acceptedPlayers = new TObjectIntHashMap<>();
	private boolean init;
	private volatile boolean end;
	private Server acceptLobby;
	private Server waitingLobby;
	private List<Variant> lobbyVariants;
	private int counter;
	private Server vipAcceptLobby;
	private Server vipWaitingLobby;
	private List<Variant> vipLobbyVariants;
	private int vipCounter;
	private int maxSlots;
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialized!");

		ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), listener);

		loadConfig();

		deployLobby(LobbyType.NORMAL, getNextVariant(), server -> acceptLobby = server);
		deployLobby(LobbyType.NORMAL, getNextVariant(), server -> waitingLobby = server);

		deployLobby(LobbyType.VIP, getNextVariant(), server -> vipAcceptLobby = server);
		deployLobby(LobbyType.VIP, getNextVariant(), server -> vipWaitingLobby = server);

		init = true;
	}

	public void loadConfig()
	{
		DeployerConfig config = Main.getInstance()
				.getDeployer().getConfig();

		List<Template.LobbyTemplate> templates = config.getLobbies();

		Optional<Template.LobbyTemplate> normal = templates.stream()
				.filter(lobby -> lobby.getType() == LobbyType.NORMAL)
				.findFirst();

		Optional<Template.LobbyTemplate> vip = templates.stream()
				.filter(lobby -> lobby.getType() == LobbyType.VIP)
				.findFirst();

		if (!normal.isPresent())
			throw new RuntimeException("No normal lobby template");
		if (!vip.isPresent())
			throw new RuntimeException("No normal vip template");

		lobbyVariants = normal.get().getVariants();
		vipLobbyVariants = vip.get().getVariants();

		maxSlots = config.getMaxSlots();
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");
		end = true;
		Main.getInstance().getLogger().info(this + " stopped.");
	}

	private Variant getNextVariant()
	{
		counter++;
		if (counter == lobbyVariants.size())
			counter = 0;

		return lobbyVariants.get(counter);
	}

	private Variant getNextVIPVariant()
	{
		vipCounter++;
		if (vipCounter == vipLobbyVariants.size())
			vipCounter = 0;

		return vipLobbyVariants.get(vipCounter);
	}

	private void changeAcceptLobby()
	{
		acceptLobby = waitingLobby;
		deployLobby(LobbyType.NORMAL, getNextVariant(), server -> waitingLobby = server);
	}

	private void changeVIPAcceptLobby()
	{
		vipAcceptLobby = vipWaitingLobby;
		deployLobby(LobbyType.VIP, getNextVIPVariant(), server -> vipWaitingLobby = server);
	}

	private void scheduleWarn(Server server)
	{
		executorService.schedule
				(
						() ->
						{
							if (!server.getServerState().canAcceptPlayers())
								return;
							server.getInfo().getPlayers().forEach(player -> player.sendMessage(WARNING));
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
							if (!server.getServerState().canAcceptPlayers())
								return;
							server.getInfo().getPlayers().forEach
									(
											player ->
											{
												player.sendMessage(SERVER_STOP);
												tryConnect(player);
											}
									);

							undeployLobby(server);
						},
						SERVER_STOP_TIME,
						TimeUnit.MILLISECONDS
				);
	}

	private boolean tryConnect(ProxiedPlayer player)
	{
		if (acceptLobby == null || !acceptLobby.getServerState().canAcceptPlayers())
		{
			player.disconnect(SERVER_STARTING);
			return false;
		}

		player.connect(acceptLobby.getInfo());
		return true;
	}

	private void deployLobby(LobbyType type, Variant v, Callback<Server> callback)
	{
		Map<String, String> labels = new HashMap<>();
		labels.put("lobbyType", type.toString());
		Main.getInstance().getDeployer().deployServer(Server.ServerType.LOBBY, v, callback, labels);
	}

	private void undeployLobby(Server server)
	{
		Main.getInstance().getDeployer().undeployServer(server);
	}

	public class Listen implements Listener
	{
		private Listen() {}

		@EventHandler
		public void onPlayerJoinLobby(PlayerConnectToServerEvent event)
		{
			System.out.println("connectToServerEvent = " + event);
			UserData data = event.getUser();

			if (ProxyServer.getInstance().getPlayers().size() >= maxSlots)
			{
				data.getBungee().disconnect(SERVER_FULL);
				return;
			}

			if ((event.getCause() == PlayerConnectToServerEvent.ConnectionCause.NETWORK_CONNECT
					&& !tryConnect(data.getBungee())) ||
					(!event.getTo().isLobby() && event.getTo() != acceptLobby))
				return;

			Server lobby = event.getTo();

			int accepted = 0;
			if (acceptedPlayers.containsKey(lobby))
				accepted = acceptedPlayers.get(lobby);

			acceptedPlayers.put(lobby, accepted + 1);

			if (accepted >= ACCEPT_PLAYERS)
				changeAcceptLobby();
		}

		@EventHandler
		public void onPlayerQuitLobby(PlayerDisconnectFromServerEvent event)
		{
			System.out.println("disconnectToServerEvent = " + event);

			Server from = event.getFrom();

			if (from == null || !from.isLobby() || from == acceptLobby || from == waitingLobby)
				return;

			int accepted = 0;
			if (acceptedPlayers.containsKey(from))
				accepted = acceptedPlayers.get(from);

			if (from.getInfo().getPlayers().isEmpty())
			{
				undeployLobby(from);
				return;
			}

			if (accepted / from.getVariant().getSlots() >= 0.75)
				scheduleWarn(from);
		}

		@EventHandler
		public void onDeployerConfigReload(DeployerConfigReloadEvent event)
		{
			loadConfig();
		}
	}
}
