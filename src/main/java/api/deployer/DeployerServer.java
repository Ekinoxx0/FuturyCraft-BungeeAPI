package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import api.config.Variant;
import api.data.Server;
import api.events.NewConsoleLineEvent;
import api.events.ServerUndeployedEvent;
import api.utils.ListBuilder;
import api.utils.UnzipUtilities;
import api.utils.Utils;
import lombok.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by loucass003 on 15/12/16.
 */
@ToString(exclude = {"server"})
public class DeployerServer implements Runnable
{
	@Getter
	protected final UUID uuid = Main.getInstance().getDataManager().newUUID();
	@Getter
	protected final String base64UUID = Utils.uuidToBase64(uuid);
	@Getter
	protected String name = '#' + base64UUID;
	@Getter
	protected final Variant variant;
	@Getter
	protected final int offset;
	@Getter
	protected final ServerType type;
	protected final File spigot;
	protected final File map;
	protected final File properties;
	@Getter
	protected Path log;
	@Getter
	protected final int port;
	protected File serverFolder;
	protected Thread currentThread;
	protected Process process;
	@Setter(AccessLevel.PACKAGE)
	protected Server server;
	protected BufferedWriter in;
	@Getter
	protected long startedTimestamp;
	@Getter
	protected boolean started;

	public DeployerServer(int offset, ServerType type, Variant variant, int port)
	{
		this.offset = offset;
		this.type = type;
		this.variant = variant;
		this.port = port;
		currentThread = new Thread(this);

		DeployerConfig c = Main.getInstance().getDeployer().getConfig();

		spigot = new File(c.getBaseDir(), variant.getSpigotPath());
		map = new File(c.getBaseDir(), variant.getMapPath());
		properties = new File(c.getBaseDir(), variant.getPropsPath());

		File typeFolder = new File(c.getDeployerDir(), getType().toString());
		File servTypeFolder = new File(typeFolder, type.toString());
		serverFolder = new File(servTypeFolder, Integer.toString(offset));
		log = new File(serverFolder, "logs/latest.log").toPath();
	}

	public ServerInfo deploy()
	{
		if (!serverFolder.exists() && !serverFolder.mkdirs())
		{
			Main.getInstance().getLogger().severe("Unable to create server folder on \"" + getName() + '"');
			return null;
		}

		UnzipUtilities unZipper = new UnzipUtilities();
		ServerInfo info = null; //no needs to catch cause already cached into #server
		try
		{
			Files.copy(spigot.toPath(), new File(serverFolder, spigot.getName()).toPath());
			unZipper.unzip(properties, serverFolder);
			unZipper.unzip(map, serverFolder);
			ProxyServer proxy = Main.getInstance().getProxy();
			info = proxy.constructServerInfo(name, new InetSocketAddress(Utils.LOCAL_HOST, port), "", false);
			proxy.getServers().put(name, info);
		}
		catch (Exception e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Unable to deploy server " + this + '.', e);
		}


		start(); //Don't forget to start ;)

		return info; //Returned info is nullable
		// #server is not initialized yet, but this ServerInfo will help the Deployer to construct a Server instance
		//  and give it to this DeployerServer
	}

	@Override
	public void run()
	{
		try
		{
			List<String> args = ListBuilder
					.<String>size(10 + variant.getJvmArgs().size() + variant.getSpigotArgs().size())
					.appendAll
							(
									"java",
									"-Xmx" + variant.getMaxRam() + 'M',
									"-Xms" + variant.getMinRam() + 'M',
									"-Dcom.mojang.eula.agree=true" // :p
							)
					.addAll(variant.getJvmArgs())
					.appendAll
							(
									"-jar",
									spigot.getAbsolutePath(),
									"-p",
									String.valueOf(port),
									"-s",
									String.valueOf(variant.getSlots())
							)
					.addAll(variant.getSpigotArgs())
					.immutable();

			ProcessBuilder pb = new ProcessBuilder(args);
			pb.directory(serverFolder);

			startedTimestamp = System.currentTimeMillis();
			started = true;

			process = pb.start();
			BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));

			in = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

			String line;
			while ((line = out.readLine()) != null && process.isAlive())
			{
				//Main.getInstance().getLogger().info(offset + ": " + line);
				ProxyServer.getInstance().getPluginManager().callEvent(new NewConsoleLineEvent(server, line));
			}

			Main.getInstance().getLogManager().saveLogs(server);
			remove();
			started = false;
			Main.getInstance().getLogger().info(this + " stopped.");

		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Unable to start server " + this + '.', e);
		}
	}

	public void remove()
	{
		try
		{
			Utils.deleteFolder(serverFolder);
		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Unable to remove server on \"" + serverFolder
					.getAbsolutePath() + '\"', e);
		}
		ProxyServer.getInstance().getPluginManager().callEvent(new ServerUndeployedEvent(server));
	}

	public void start()
	{
		if (currentThread != null)
		{
			currentThread.start();
			Main.getInstance().getLogger().info("Server " + this + " started.");
		}
	}

	public void kill()
	{
		if (process != null)
			process.destroy();
		if (currentThread != null)
			currentThread.interrupt();
	}

	public String getConsole()
	{
		try
		{
			return Files.readAllLines(log).stream().collect(Collectors.joining("\n"));
		}
		catch (IOException e)
		{
			Main.getInstance().getLogger().log(Level.SEVERE, "Unable to get console from server " + server, e);
			return "Internal Error :(";
		}
	}

	public void sendCommand(String command)
	{
		try
		{
			in.write(command + '\n');
			in.flush();
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	@Getter
	@AllArgsConstructor
	public enum ServerType
	{
		LOBBY("Lobby"),
		GAME("Game");

		private final String name;

		@Override
		public String toString()
		{
			return name;
		}
	}
}
