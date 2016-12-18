package api.deployer;

import api.Main;
import api.config.ServerConfig;
import api.utils.UnzipUtilities;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

/**
 * Created by loucass003 on 15/12/16.
 */
public class Server implements Runnable {
    private final ServerConfig config;
    public String name;
    public ServerType type;
    public File spigot;
    public File map;
    public File properties;
    public int port;
    public int slots;
    public File serverFolder;
    public Thread crrentThread;

    public enum ServerType {
        LOBBY,
        GAME
    }

    public Server(ServerType type, ServerConfig config, int port, int slots) {
        this.type = type;
        this.config = config;
        this.port = port;
        this.slots = slots;
        this.crrentThread = new Thread(this);


        this.spigot = new File(config.getSpigotPath());
        this.map = new File(config.getSpigotPath());
        this.properties = new File(config.getSpigotPath());
    }

    public void deploy()
    {
        if (!serverFolder.exists())
        {
            if (!serverFolder.mkdirs())
            {
                Main.getInstance().getLogger().severe("Unable to create server folder on \"" + getName() + "\"");
                return;
            }
        }

        UnzipUtilities unzipper = new UnzipUtilities();
        try
        {
            Files.copy(spigot.toPath(), serverFolder.toPath());
            unzipper.unzip(properties, serverFolder);
            File serverProps = new File(serverFolder, "server.properties");
            if (!serverProps.exists())
            {
                Main.getInstance().getLogger().severe("Unable to edit server.properties file on \"" + getName() + "\"");
                return;
            }

            ProxyServer proxy = Main.getInstance().getProxy();
            proxy.getServers().put(name, proxy.constructServerInfo(name, Util.getAddr("127.0.0.1"), getName(), false));

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try
        {
            String jvmArgs = String.format("-Xmx%d ", config.getMaxRam()) +
                    String.format("-Xms%d ", config.getMinRam()) +
                    config.getJvmArgs() +
                    " -jar";
            String spigotArgs = spigot.getAbsolutePath() +
                    " --p " + getPort() +
                    " --s " + getSlots() +
                    " --W " + getMap().getAbsolutePath() +
                    " " + config.getSpigotArgs();

            ProcessBuilder pb = new ProcessBuilder("java", jvmArgs, spigotArgs);
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s;
            while ((s = in.readLine()) != null)
            {
                System.out.println(s);
            }
            int status = p.waitFor();
            System.out.println("Exited with status: " + status);
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void start() {
        this.crrentThread.start();
    }

    public String getName() {
        return name;
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

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }


}
