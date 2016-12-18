package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import api.config.ServerConfig;
import api.utils.Utils;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by loucass003 on 14/12/16.
 */
public class Deployer implements Listener
{

    public Main main;
    public List<Server> servers;
    public DeployerConfig config;
    public File deployerFolder;

    public int MIN_PORT = 12000;
    public int MAX_PORT = 25000;

    public Deployer(Main main)
    {
        this.main = main;
        this.servers = new ArrayList<>();
    }

    public void init()
    {
        main.getProxy().getPluginManager().registerListener(main, this);
        this.config = DeployerConfig.getConfig();
        if(this.config == null)
        {
            Main.getInstance().getLogger().severe("Unable to find/read the deployer config.");
            return;
        }

        this.deployerFolder = new File(this.config.deployerFolder);
        try
        {
            Utils.delete(deployerFolder);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        for(Lobby.LobbyType t : Lobby.LobbyType.values())
        {
            int count = 0;
            for(int i = 0; i < t.minCount; i++)
            {
                List<ServerConfig> configs = this.config.getServersTemplates().get(Server.ServerType.LOBBY).get(t.toString());
                if(configs.size() == 0)
                    break;
                if(count >= configs.size())
                    count = 0;
                ServerConfig config = configs.get(count++);
                int nextport = this.getNextPort();
                if(nextport == -1)
                    break;
                Lobby server = new Lobby(i, t, config, nextport);
                server.deploy();
                this.servers.add(server);
            }
        }
    }

    public int getNextPort()
    {
        List<Integer> ports = servers.stream().map(Server::getPort).collect(Collectors.toList());
        return Stream.iterate(MIN_PORT, port -> port + 1).limit(MAX_PORT).
                filter(i -> !ports.contains(i)).
                filter(i -> isReachable(i, "127.0.0.1")).
                findFirst().orElse(-1);
    }

    public boolean isReachable(int port, String hostName)
    {
        try (Socket ignored = new Socket(hostName, port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }

    public Server getServer(int port)
    {
        for(Server s : servers)
            if(s.getPort() == port)
                return s;
        return null;
    }

    public void clear()
    {
        this.servers.clear();
    }

    public int countLobby(Lobby.LobbyType t)
    {
        int count = 0;
        for(Server s : servers)
            if(s.getType() == Server.ServerType.LOBBY && ((Lobby)s).getLobbyType() == t)
                count++;
        return count;
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e)
    {

    }

    @EventHandler
    public void onPlayerQuit(PlayerDisconnectEvent e)
    {

    }
}
