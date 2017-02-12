package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import api.config.Variant;
import api.data.Server;
import api.utils.SimpleManager;
import api.utils.Utils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;

/**
 * Created by loucass003 on 14/12/16.
 */
@ToString
public final class Deployer implements SimpleManager
{
	private static final int MIN_PORT = 12000;
	private static final int MAX_PORT = 25000;
	private static final int MAX_SERVERS = MAX_PORT - MIN_PORT;

	@Getter
	private DeployerConfig config;
	@Getter
	private DockerClient dockerClient;

	private boolean init;
	private volatile boolean end;

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialised!");

		config = DeployerConfig.load(new File(Main.getInstance().getDataFolder(), "deployer.json"));
		try
		{
			Utils.deleteFolder(config.getDeployerDir());
			if (!config.getDeployerDir().exists() && !config.getDeployerDir().mkdirs())
			{
				Main.getInstance().getLogger().log(Level.SEVERE, "Unable to mkdirs (Deployer: " + this + ')');
				return;
			}
		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Error while initializing the Deployer (Deployer: " + this + ')', e);
		}

		dockerClient =  DockerClientBuilder.getInstance("tcp://localhost:2375").build();

		init = true;
	}

	public Server deployServer(Server.ServerType type, Variant v)
	{
		ExposedPort tcpMc = ExposedPort.tcp(25565);
		Ports portBindings = new Ports();
		int port = getNextPort();
		portBindings.bind(tcpMc, Ports.Binding.bindPort(port));

		CreateContainerCmd cmd = dockerClient.createContainerCmd(v.getImg())
				.withMemory((long)v.getMaxRam())
				.withExposedPorts(tcpMc)
				.withPortBindings(portBindings);

		CreateContainerResponse container = cmd.exec();

		dockerClient.startContainerCmd(container.getId()).exec();
		InspectContainerResponse inspect = dockerClient.inspectContainerCmd(container.getId()).exec();
		String host = inspect.getNetworkSettings().getPorts().getBindings().get(tcpMc)[0].getHostIp();
		ServerInfo s = ProxyServer.getInstance().constructServerInfo(container.getId(), new InetSocketAddress(host, port), "", false);
		return new Server(container.getId(), type, v, s);
	}

	public void initServers()
	{
		if(!init)
			throw new IllegalStateException("Deployer not initialised!");

	}

	public int getNextPort()
	{
		return Main.getInstance().getDataManager().getNextDeployerPort(MIN_PORT, MAX_PORT);
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");
		//TODO: kill containers
		Main.getInstance().getLogger().info(this + " stopped.");
	}
}
