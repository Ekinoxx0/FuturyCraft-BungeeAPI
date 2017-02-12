package api.data;

import api.Main;
import api.deployer.ServerState;
import api.events.ServerChangeStateEvent;
import api.packets.MessengerClient;
import api.packets.server.ServerStatePacket;
import api.utils.SimpleManager;
import api.utils.Utils;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used to save players' data to Redis and Mongo.
 * <p>
 * Created by SkyBeast on 19/12/2016.
 */
@ToString
public final class DataManager implements SimpleManager
{
	private final ExecutorService exec = Executors.newSingleThreadExecutor();
	private final List<Server> servers = new ArrayList<>();
	private final ReentrantLock serversLock = new ReentrantLock();
	private final AtomicInteger serverCount = new AtomicInteger();
	private boolean init;
	private volatile boolean end;

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialised!");
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

	/**
	 * Find a server by its port.
	 *
	 * @param port the port of the server
	 * @return the server
	 */
	public Server findServerByPort(int port)
	{
		return Utils.returnLocked
				(
						() -> servers.stream()
								.filter(srv -> srv.getInfo().getAddress().getPort() == port)
								.findAny().orElse(null),
						serversLock
				);
	}

	/**
	 * Do whatever you want with the servers, but safely.
	 *
	 * @param consumer what to do
	 */
	public void forEachServers(Consumer<? super Server> consumer)
	{
		Utils.doLocked
				(
						() -> servers.forEach(consumer),
						serversLock
				);
	}

	/**
	 * Do whatever you want with the servers, but safely.
	 *
	 * @param consumer what to do
	 * @param type type filter
	 */
	public void forEachServersByType(Consumer<? super Server> consumer, Server.ServerType type)
	{
		Utils.doLocked
				(
						() -> servers.stream()
								.filter(server -> server.getType() == type)
								.forEach(consumer),
						serversLock
				);
	}

	/**
	 * Get the next deployer port.
	 *
	 * @param minPort the min bound
	 * @param maxPort the max bound
	 * @return the next deployer port
	 */
	public int getNextDeployerPort(int minPort, int maxPort)
	{
		return Utils.returnLocked
				(
						() ->
						{
							List<Integer> ports = servers.stream()
									.map(server -> server.getInfo().getAddress().getPort())
									.collect(Collectors.toList());
							return Stream.iterate(minPort, port -> port + 1)
									.limit(maxPort)
									.filter(i -> !ports.contains(i))
									.findFirst().orElse(-1);
						},
						serversLock
				);
	}

	/**
	 * Get a server from the cached online server list.
	 *
	 * @param info the info of the server
	 * @return the server
	 */
	public Server getServer(ServerInfo info)
	{
		if (info == null) return null;

		return Utils.returnLocked
				(
						() -> servers.stream()
								.filter(server -> server.getInfo().equals(info))
								.findFirst()
								.orElse(null),
						serversLock
				);
	}

	/**
	 * Unregister a server from the cached online server list.
	 *
	 * @param server the server to remove
	 */
	public void unregisterServer(Server server)
	{
		serverCount.getAndIncrement();
		Utils.doLocked(() -> servers.remove(server), serversLock);
	}

	/**
	 * Get the server count.
	 *
	 * @return the server count
	 */
	public int getServerCount()
	{
		return serverCount.get();
	}

	/**
	 * Update a server's MessengerClient.
	 *
	 * @param srv    the server to update
	 * @param client the new client
	 */
	public void updateMessenger(Server srv, MessengerClient client)
	{
		srv.setMessenger(client);
	}

	/**
	 * Update a server's state.
	 *
	 * @param srv   the server to update
	 * @param state the new state
	 */
	public void updateServerState(Server srv, ServerState state)
	{
		ProxyServer.getInstance().getPluginManager().callEvent(new ServerChangeStateEvent(srv, state));
		srv.setServerState(state);
	}
}
