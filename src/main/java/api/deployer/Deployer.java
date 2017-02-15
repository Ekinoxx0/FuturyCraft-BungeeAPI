package api.deployer;

import api.Main;
import api.config.ConfigVolume;
import api.config.DeployerConfig;
import api.config.ServerPattern;
import api.config.Variant;
import api.data.Server;
import api.utils.SimpleManager;
import api.utils.concurrent.Callback;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
	private final ExecutorService exec = Executors.newCachedThreadPool();

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
		dockerClient = DockerClientBuilder.getInstance("unix:///var/run/docker.sock").build();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> Main.getInstance().getServerDataManager().forEachServers(this::kill)));
		init = true;
	}

	public void deployServer(ServerPattern pattern, Callback<Server> callback)
	{
		exec.submit(() ->
				{
					try
					{
						ExposedPort tcpMc = ExposedPort.tcp(25565);
						Ports portBindings = new Ports();
						int port = getNextPort();
						portBindings.bind(tcpMc, Ports.Binding.bindPort(port));

						UUID uuid = UUID.randomUUID();

						File folder = new File(getConfig().getDeployerDir(), uuid.toString());
						if(!folder.exists() && !folder.mkdir())
							throw new IllegalStateException("Unable to cretate tmp folder for container " + folder.getPath());

						pattern.getLabels().put("uuid", uuid.toString());
						CreateContainerCmd cmd = dockerClient.createContainerCmd(pattern.getVariant().getImg())
								.withMemory(pattern.getVariant().getMaxRam() * (long)Math.pow(1024, 2))
								.withExposedPorts(tcpMc)
								.withPortBindings(portBindings)
								.withLabels(pattern.getLabels());

						List<Volume> volumes = new ArrayList<>();
						List<Bind> binds = new ArrayList<>();
						pattern.getVariant().getVolumes().forEach(cv ->
						{
							Volume volume = new Volume(cv.getContainer());
							volumes.add(volume);
							binds.add(prepareVolume(folder, uuid, cv, volume));
						});
						cmd.withVolumes(volumes);
						cmd.withBinds(binds);

						CreateContainerResponse container = cmd.exec();
						System.out.println("create container -> " + container.getId());
						dockerClient.startContainerCmd(container.getId()).exec();
						InspectContainerResponse inspect = dockerClient.inspectContainerCmd(container.getId()).exec();
						String host = inspect.getNetworkSettings().getPorts().getBindings().get(tcpMc)[0].getHostIp();
						ServerInfo s = ProxyServer.getInstance().constructServerInfo(container.getId(), new InetSocketAddress(host, port), "", false);
						ProxyServer.getInstance().getServers().put(container.getId(), s);
						Server server = new Server(container.getId(), pattern, folder, s);
						Main.getInstance().getServerDataManager().registerServer(server);

						if (callback != null)
							callback.response(server);
					}
					catch (Throwable e)
					{
						e.printStackTrace();
					}
				}
		);
	}

	public Bind prepareVolume(File folder, UUID uuid, ConfigVolume cv, Volume volume)
	{
		File to = new File(folder, cv.getContainer());
		if(!to.getParentFile().exists())
			to.getParentFile().mkdirs();
		try
		{
			FileUtils.copyDirectory(cv.getHost(), to);
		}
		catch (IOException ignored)
		{
			throw new IllegalStateException("Unable to copy volume");
		}

		return new Bind(to.getAbsolutePath(), volume, cv.isReadOnly() ? AccessMode.ro : AccessMode.rw);
	}

	public void undeployServer(Server s)
	{
		Main.getInstance().getLogger().log(Level.INFO, "undeploy server with id -> " + s.getId());
		Main.getInstance().getServerDataManager().unregisterServer(s);
		kill(s);
	}

	public void kill(Server s)
	{
		dockerClient.killContainerCmd(s.getId()).exec();
		dockerClient.removeContainerCmd(s.getId()).exec();
		if(s.getTempFolder().exists())
		{
			try
			{
				FileUtils.deleteDirectory(s.getTempFolder());
			}
			catch (IOException e)
			{
				Main.getInstance().getLogger().log(Level.SEVERE, "Unable to remove folder :" + s.getTempFolder().getPath(), e);
			}
		}
	}

	public int getNextPort()
	{
		return Main.getInstance().getServerDataManager().getNextDeployerPort(MIN_PORT, MAX_PORT);
	}

	@Override
	public void stop()
	{
		if (end)
			throw new IllegalStateException("Already ended!");
		Main.getInstance().getServerDataManager().forEachServers(this::kill);
		Main.getInstance().getLogger().info(this + " stopped.");
	}
}
