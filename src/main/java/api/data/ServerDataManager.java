package api.data;

import api.Main;
import api.deployer.ServerState;
import api.events.ServerChangeStateEvent;
import api.utils.SimpleManager;
import lombok.ToString;
import lombok.extern.java.Log;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Used to save players' data MongoDB.
 * <p>
 * Created by SkyBeast on 19/12/2016.
 */
@ToString
@Log
public final class ServerDataManager implements SimpleManager
{
	private final ExecutorService exec = Executors.newSingleThreadExecutor();
	private final List<Server> servers = new CopyOnWriteArrayList<>();
	private final List<Short> ports = new CopyOnWriteArrayList<>();

	public static ServerDataManager instance()
	{
		return Main.getInstance().getServerDataManager();
	}

	/**
	 * Find a server by its port.
	 *
	 * @param port the port of the server
	 * @return the server
	 */
	public Server findServerByPort(int port)
	{
		return servers.stream()
				.filter(srv -> srv.getInfo().getAddress().getPort() == port)
				.findAny().orElse(null);
	}

	/**
	 * Do whatever you want with the servers, but safely.
	 *
	 * @param consumer what to do
	 */
	public void forEachServers(Consumer<? super Server> consumer)
	{
		servers.forEach(consumer);
	}

	/**
	 * Do whatever you want with the servers, but safely.
	 *
	 * @param consumer what to do
	 * @param type     type filter
	 */
	public void forEachServersByType(Consumer<? super Server> consumer, String type)
	{
		servers.stream()
				.filter(server -> server.getPattern().has("type", type))
				.forEach(consumer);
	}

	/**
	 * Get the next deployer port.
	 *
	 * @param minPort the min bound
	 * @param maxPort the max bound
	 * @return the next deployer port
	 */
	public synchronized int getNextDeployerPort(int minPort, int maxPort)
	{
		int port = IntStream.range(minPort, maxPort)
				.filter(i -> !ports.contains((short) i))
				.findFirst()
				.orElseThrow(
						() -> new IllegalStateException("Unable to find port!")
				);
		registerPort(port);
		return port;
	}

	public void registerPort(int port)
	{
		ports.add((short) port);
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

		return servers.stream()
				.filter(server -> server.getInfo().equals(info))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Get a server from its container id.
	 *
	 * @param id the container id
	 * @return the server
	 */
	public Server getServer(String id)
	{
		if (id == null) return null;

		return servers.stream()
				.filter(server -> server.getId().equals(id))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Register a server.
	 *
	 * @param server the server to add
	 */
	public void registerServer(Server server)
	{
		servers.add(server);
	}

	/**
	 * Unregister a server from the cached online server list.
	 *
	 * @param server the server to remove
	 */
	public void unregisterServer(Server server)
	{
		servers.remove(server);
		ports.remove((short) server.getInfo().getAddress().getPort());
	}

	/**
	 * Get the server count.
	 *
	 * @return the server count
	 */
	public int getServerCount()
	{
		return servers.size();
	}

	/**
	 * Update a server's state.
	 *
	 * @param srv   the server to update
	 * @param state the new state
	 */
	public void updateServerState(Server srv, ServerState state)
	{
		Main.callEvent(new ServerChangeStateEvent(srv, state));
		srv.setServerState(state);
	}
}
