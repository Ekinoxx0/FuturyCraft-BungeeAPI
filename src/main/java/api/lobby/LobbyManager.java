package api.lobby;

import api.Main;
import api.config.ServerConfig;
import api.config.ServerPattern;
import api.config.Variant;
import api.data.Server;
import api.data.UserData;
import api.deployer.Deployer;
import api.deployer.ServerState;
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
		Main.registerListener(listener);

		loadConfig();

		deployLobby(false, server -> acceptLobby = server);
		deployLobby(false, server -> waitingLobby = pause(server));

		deployLobby(true, server -> vipAcceptLobby = server);
		deployLobby(true, server -> vipWaitingLobby = pause(server));
	}

	/**
	 * Pause a server.
	 *
	 * @param server the server
	 * @return the server
	 */
	private Server pause(Server server)
	{
		server.pause();
		return server;
	}

	/**
	 * Load the configuration.
	 */
	private void loadConfig()
	{
		serverConfig = getConfig("normal");
		vipServerConfig = getConfig("vip");

		maxSlots = Deployer.instance().getConfig().getMaxSlots();
	}

	/**
	 * Get the configuration for a lobby type.
	 *
	 * @param lobbyType the lobby type
	 * @return the server configuration
	 */
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

	/**
	 * Get the next variant for normal lobbies.
	 *
	 * @return the next variant
	 */
	private Variant getNextVariant()
	{
		counter++;
		if (counter == serverConfig.getVariants().size())
			counter = 0;

		return serverConfig.getVariants().get(counter);
	}

	/**
	 * Get the next variant for VIP lobbies.
	 *
	 * @return the next variant
	 */
	private Variant getNextVIPVariant()
	{
		vipCounter++;
		if (vipCounter == vipServerConfig.getVariants().size())
			vipCounter = 0;

		return vipServerConfig.getVariants().get(vipCounter);
	}

	/**
	 * Change accepting lobby, waiting lobby.
	 */
	private void changeAcceptLobby()
	{
		waitingLobby.unPause();
		acceptLobby = waitingLobby;
		deployLobby(false, server -> waitingLobby = pause(server));
	}

	/**
	 * Change accepting vip lobby, vip waiting lobby.
	 */
	private void changeVIPAcceptLobby()
	{
		vipWaitingLobby.unPause();
		vipAcceptLobby = vipWaitingLobby;
		deployLobby(true, server -> vipWaitingLobby = pause(server));
	}

	/**
	 * Schedule the warning message for a server.
	 *
	 * @param server the server
	 */
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

	/**
	 * Schedule the stop task for a server.
	 *
	 * @param server the server
	 */
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

	/**
	 * Try to connect a player to acceptLobby.
	 *
	 * @param player the player
	 * @return true if connected
	 */
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

	/**
	 * Deploy a new lobby.
	 *
	 * @param vip      true if the lobby will be VIP
	 * @param callback the callback which will be called when the server is ready to accept players
	 */
	private void deployLobby(boolean vip, Callback<Server> callback)
	{
		Deployer.instance()
				.deployServer(vip ? ServerPattern.of(vipServerConfig, getNextVIPVariant()) :
						ServerPattern.of(serverConfig, getNextVariant()), callback, ServerState.READY);
	}

	private void undeployLobby(Server server)
	{
		Deployer.instance().undeployServer(server);
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
