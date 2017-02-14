package api.lobby;

import api.Main;
import api.config.ServerConfig;
import api.config.ServerPattern;
import api.config.Variant;
import api.data.Server;
import api.data.UserData;
import api.events.DeployerConfigReloadEvent;
import api.events.PlayerConnectToServerEvent;
import api.events.PlayerDisconnectFromServerEvent;
import api.utils.MapBuilder;
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
	private ServerConfig serverConfig;
	private int counter;
	private Server vipAcceptLobby;
	private Server vipWaitingLobby;
	private ServerConfig vipServerConfig;
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

		deployLobby(false, server -> acceptLobby = server);
		deployLobby(false, server -> waitingLobby = server);

		deployLobby(true, server -> vipAcceptLobby = server);
		deployLobby(true, server -> vipWaitingLobby = server);

		init = true;
	}

	public void loadConfig()
	{
		serverConfig = getConfig("normal");
		vipServerConfig = getConfig("vip");

		maxSlots = Main.getInstance()
				.getDeployer().getConfig().getMaxSlots();
	}

	private ServerConfig getConfig(String lobbyType)
	{
		List<ServerConfig> configs = ServerConfig.getByLabels(
				MapBuilder.mapOf(String.class, String.class,
						"type", "lobby",
						"lobbyType", lobbyType));

		if (configs.isEmpty())
			throw new IllegalArgumentException("Cannot find vip config");

		return configs.get(0);
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
		if (counter == serverConfig.getVariants().size())
			counter = 0;

		return serverConfig.getVariants().get(counter);
	}

	private Variant getNextVIPVariant()
	{
		vipCounter++;
		if (vipCounter == vipServerConfig.getVariants().size())
			vipCounter = 0;

		return vipServerConfig.getVariants().get(vipCounter);
	}

	private void changeAcceptLobby()
	{
		acceptLobby = waitingLobby;
		deployLobby(false, server -> waitingLobby = server);
	}

	private void changeVIPAcceptLobby()
	{
		vipAcceptLobby = vipWaitingLobby;
		deployLobby(true, server -> vipWaitingLobby = server);
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

	private void deployLobby(boolean vip, Callback<Server> callback)
	{
		Main.getInstance().getDeployer()
				.deployServer(vip ? ServerPattern.of(vipServerConfig, getNextVIPVariant()) :
						ServerPattern.of(serverConfig, getNextVariant()), callback);
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

			if (accepted / from.getPattern().getVariant().getSlots() >= 0.75)
				scheduleWarn(from);
		}

		@EventHandler
		public void onDeployerConfigReload(DeployerConfigReloadEvent event)
		{
			loadConfig();
		}
	}
}
