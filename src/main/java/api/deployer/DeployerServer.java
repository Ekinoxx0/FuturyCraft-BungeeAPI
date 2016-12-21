package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import api.config.ServerTemplate;
import api.utils.UnzipUtilities;
import api.utils.Utils;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

/**
 * Created by loucass003 on 15/12/16.
 */
public class DeployerServer implements Runnable
{
    private final ServerTemplate template;
    private String name;
    private int id;
    private ServerType type;
    private File spigot;
    private File map;
    private File properties;
    private int port;
    private File serverFolder;
    private Thread currentThread;
    private Process process;
    private ServerInfo info;

    public enum ServerType {
        LOBBY,
        GAME
    }

    public DeployerServer(int id, String name, ServerType type, ServerTemplate template, int port)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.template = template;
        this.port = port;
        this.currentThread = new Thread(this);

        this.spigot = new File(DeployerConfig.getBaseDir(), template.getSpigotPath());
        this.map = new File(DeployerConfig.getBaseDir(), template.getMapPath());
        this.properties = new File(DeployerConfig.getBaseDir(), template.getPropsPath());

        File typeFolder = new File(DeployerConfig.getDeployerDir(), getType().toString());
        File servTypeFolder = new File(typeFolder, name);
        this.setServerFolder(new File(servTypeFolder, Integer.toString(getId())));
    }

    public ServerInfo deploy()
    {
        if (!serverFolder.exists())
        {
            if (!serverFolder.mkdirs())
            {
                Main.getInstance().getLogger().severe("Unable to create server folder on \"" + getName() + "\"");
                return null;
            }
        }

        UnzipUtilities unZipper = new UnzipUtilities();
        try
        {
            Files.copy(spigot.getAbsoluteFile().toPath(), new File(serverFolder, spigot.getName()).toPath());
            unZipper.unzip(properties, serverFolder);
            ProxyServer proxy = Main.getInstance().getProxy();
            info = proxy.constructServerInfo(name, Util.getAddr("127.0.0.1"), getName(), false);
            proxy.getServers().put(name, info);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return info;
    }

    @Override
    public void run()
    {
        try
        {
            String jvmArgs = String.format("-Xmx%d ", this.template.getMaxRam()) +
                    String.format("-Xms%d ", this.template.getMinRam()) +
                    this.template.getJvmArgs() +
                    " -jar";
            String spigotArgs = this.spigot.getAbsolutePath() +
                    " --p " + this.getPort() +
                    " --s " + this.template.getSlots() +
                    " --W " + this.getMap().getAbsolutePath() +
                    " " + this.template.getSpigotArgs();

            ProcessBuilder pb = new ProcessBuilder("java", jvmArgs, spigotArgs);
            this.process = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
            String s;
            while ((s = in.readLine()) != null)
            {
                //TODO: Packet log;
            }
            int status = this.process.waitFor();
            //TODO: Save stop status;
            remove();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void remove()
    {
        Thread t = new Thread(() ->
        {
            try
            {
                Utils.deleteFolder(this.serverFolder);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Main.getInstance().getLogger().severe("Unable to remove server on \"" + this.serverFolder.getAbsolutePath() + "\"");
            }
        });
        t.start();
    }

    public void start() {
        this.currentThread.start();
    }

    public void kill()
    {
        this.process.destroy();
        this.currentThread.interrupt();
    }

    public String getName()
    {
        return "SERVER " + this.name + "#" + this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getSpigot() {
        return spigot;
    }

    public void setSpigot(File spigot) {
        this.spigot = spigot;
    }

    public File getProperties() {
        return properties;
    }

    public void setProperties(File properties) {
        this.properties = properties;
    }

    public File getMap() {
        return map;
    }

    public void setMap(File map) {
        this.map = map;
    }

    public ServerType getType() {
        return type;
    }

    public void setType(ServerType type) {
        this.type = type;
    }

    public int getPort() {
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

    public ServerInfo getInfo()
    {
        return info;
    }
}
