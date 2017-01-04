package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import api.config.Variant;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by loucass003 on 15/12/16.
 */
public class DeployerServer implements Runnable
{
	private final Variant variant;
	private int id;
	private ServerType type;
	private File spigot;
	private File map;
	private File properties;
	private int port;
	private File serverFolder;
	private Thread currentThread;
	private Process process;
	protected String name;

	public enum ServerType
	{
		LOBBY,
		GAME
	}

	public DeployerServer(int id, ServerType type, Variant variant, int port)
	{
		this.id = id;
		this.type = type;
		this.variant = variant;
		this.port = port;
		this.currentThread = new Thread(this);
		this.name = "SERVER#" + this.id;

		DeployerConfig c = Main.getInstance().getDeployer().getConfig();

		this.spigot = new File(c.getBaseDir(), variant.getSpigotPath());
		this.map = new File(c.getBaseDir(), variant.getMapPath());
		this.properties = new File(c.getBaseDir(), variant.getPropsPath());

		File typeFolder = new File(c.getDeployerDir(), getType().toString());
		File servTypeFolder = new File(typeFolder, type.toString());
		this.setServerFolder(new File(servTypeFolder, Integer.toString(getId())));
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
			/*String jvmArgs = String.format("-Xmx%d ", this.variant.getMaxRam()) +
					String.format("-Xms%d ", this.variant.getMinRam()) +
					this.variant.getJvmArgs() +
					" -jar";
			String spigotArgs = this.spigot.getAbsolutePath() +
					" --p " + this.getPort() +
					" --s " + this.variant.getSlots() +
					" --W " + this.getMap().getAbsolutePath() +
					" " + this.variant.getSpigotArgs();*/

			List<String> args = new ArrayList<>();
			args.add("java");
			args.add("-Xmx" + variant.getMaxRam() + "M");
			args.add("-Xms" + variant.getMinRam() + "M");
			args.add("-Dcom.mojang.eula.agree=true"); // :p
			variant.getJvmArgs().forEach(args::add);
			args.add("-jar");

			args.add(spigot.getAbsolutePath());
			args.add("-p");
			args.add(String.valueOf(port));
			args.add("-s");
			args.add(String.valueOf(variant.getSlots()));
			variant.getSpigotArgs().forEach(args::add);

			ProcessBuilder pb = new ProcessBuilder(args);
			pb.directory(serverFolder);
			this.process = pb.start();
			BufferedReader in = new BufferedReader(new InputStreamReader(this.process.getInputStream()));

			String s;
			while ((s = in.readLine()) != null && process.isAlive())
			{
				Main.getInstance().getLogger().info(id + ": " + s);
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

	public String getName()
	{
		return name;
	}

	public File getSpigot()
	{
		return spigot;
	}

	public void setSpigot(File spigot)
	{
		this.spigot = spigot;
	}

	public File getProperties()
	{
		return properties;
	}

	public void setProperties(File properties)
	{
		this.properties = properties;
	}

	public File getMap()
	{
		return map;
	}

	public void setMap(File map)
	{
		this.map = map;
	}

	public ServerType getType()
	{
		return type;
	}

	public void setType(ServerType type)
	{
		this.type = type;
	}

	public int getPort()
	{
		return port;
	}

	public File getServerFolder()
	{
		return serverFolder;
	}

	public void setServerFolder(File serverFolder)
	{
		this.serverFolder = serverFolder;
	}

	public int getId()
	{
		return id;
	}

	public Variant getVariant()
	{
		return variant;
	}

	@Override
	public String toString()
	{
		return "DeployerServer{" +
				"variant=" + variant +
				", id=" + id +
				", type=" + type +
				", spigot=" + spigot +
				", map=" + map +
				", properties=" + properties +
				", port=" + port +
				", serverFolder=" + serverFolder +
				", currentThread=" + currentThread +
				", process=" + process +
				'}';
	}
}
