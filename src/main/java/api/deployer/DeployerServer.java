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
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class DeployerServer implements Runnable
{
	protected final UUID uuid = Main.getInstance().getDataManager().newUUID();
	protected final String base64UUID = Utils.formatToUUID(uuid);
	protected String name = "#" + base64UUID;
	protected final Variant variant;
	protected final int offset;
	protected final ServerType type;
	protected final File spigot;
	protected final File map;
	protected final File properties;
	protected final Path log;
	protected final int port;
	protected File serverFolder;
	protected Thread currentThread;
	protected Process process;
	protected Server server;

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
		ServerInfo info = null; //no needs to catch cause already cached into #server
		if (!serverFolder.exists() && !serverFolder.mkdirs())
		{
			Main.getInstance().getLogger().severe("Unable to create server folder on \"" + getName() + "\"");
			return null;
		}

		UnzipUtilities unZipper = new UnzipUtilities();
		try
		{
			Files.copy(spigot.toPath(), new File(serverFolder, spigot.getName()).toPath());
			unZipper.unzip(properties, serverFolder);
			unZipper.unzip(map, serverFolder);
			ProxyServer proxy = Main.getInstance().getProxy();
			info = proxy.constructServerInfo(name, new InetSocketAddress(Utils.LOCAL_HOST, port), "", false);
			proxy.getServers().put(name, info);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
					.of
							(
									"java",
									"-Xmx" + variant.getMaxRam() + "M",
									"-Xms" + variant.getMinRam() + "M",
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
			process = pb.start();
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = in.readLine()) != null && process.isAlive())
			{
				Main.getInstance().getLogger().info(offset + ": " + line);
				ProxyServer.getInstance().getPluginManager().callEvent(new NewConsoleLineEvent(server, line));
			}

			remove();

			Main.getInstance().getLogger().info(this + " stopped.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void remove()
	{
		try
		{
			Utils.deleteFolder(this.serverFolder);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			Main.getInstance().getLogger().severe("Unable to remove server on \"" + this.serverFolder
					.getAbsolutePath() + "\"");
		}

		Server srv = Main.getInstance().getDataManager().getServer(base64UUID);
		Main.getInstance().getDataManager().unregisterServer(srv);
		ProxyServer.getInstance().getPluginManager().callEvent(new ServerUndeployedEvent(srv));
	}

	public void start()
	{
		if (currentThread != null)
		{
			this.currentThread.start();
			Main.getInstance().getLogger().info("Server " + this + " started.");
		}
	}

	public void kill()
	{
		if (process != null)
			this.process.destroy();
		if (currentThread != null)
			this.currentThread.interrupt();
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
			return "";
		}
	}

	public String getName()
	{
		return name;
	}

	public File getSpigot()
	{
		return spigot;
	}

	public File getProperties()
	{
		return properties;
	}

	public File getMap()
	{
		return map;
	}

	public ServerType getType()
	{
		return type;
	}

	public int getPort()
	{
		return port;
	}

	public File getServerFolder()
	{
		return serverFolder;
	}

	public int getOffset()
	{
		return offset;
	}

	public Variant getVariant()
	{
		return variant;
	}

	public UUID getServerUUID()
	{
		return uuid;
	}

	public String getServerBase64UUID()
	{
		return base64UUID;
	}

	void setServer(Server server)
	{
		this.server = server;
	}

	@Override
	public String toString()
	{
		return "DeployerServer{" +
				"uuid=" + uuid +
				", base64UUID='" + base64UUID + '\'' +
				", name='" + name + '\'' +
				", variant=" + variant +
				", offset=" + offset +
				", type=" + type +
				", spigot=" + spigot +
				", map=" + map +
				", properties=" + properties +
				", log=" + log +
				", port=" + port +
				", serverFolder=" + serverFolder +
				", currentThread=" + currentThread +
				", process=" + process +
				'}';
	}

	public enum ServerType
	{
		LOBBY("Lobby"),
		GAME("Game");

		private String name;

		ServerType(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
