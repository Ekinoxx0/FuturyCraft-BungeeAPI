package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import api.config.ServerConfig;
import api.config.ServerTemplate;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by loucass003 on 14/12/16.
 */
public class Deployer implements Listener
{

    public Main main;
    public List<DeployerServer> deployerServers;
    public DeployerConfig config;


    public int MIN_PORT = 12000;
    public int MAX_PORT = 25000;
    public int MAX_SERVERS = MAX_PORT - MIN_PORT;

    public Deployer(Main main)
    {
        this.main = main;
        this.deployerServers = new ArrayList<>();
        this.config = new DeployerConfig();
    }

    public void init()
    {
        config.load(new File(Main.getInstance().getDataFolder(), "deployer.yml"));
        try
        {
            Utils.deleteFolder(DeployerConfig.getDeployerDir());
            DeployerConfig.getDeployerDir().mkdir();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        for (Map.Entry<String, Map<String, Object>> entry : config.getServers().entrySet())
        {

            ServerConfig srv_conf = new ServerConfig(entry.getKey(), entry.getValue());
            for(ServerTemplate template : srv_conf.getTemplates())
            {
                for(int i = 0; i < template.getMinServers(); i++)
                {
                    DeployerServer server;
                    int id = getNextId();
                    int port = getNextPort();
                    if(srv_conf.getType() == DeployerServer.ServerType.LOBBY)
                    {
                        server = new Lobby(id, srv_conf.getName(), template, port);
                    }
                    else
                        server = new DeployerServer(id, srv_conf.getName(), srv_conf.getType(), template, port);
                    server.deploy();
                }
            }
        }
    }

    public int getNextId()
    {
        List<Integer> ports = deployerServers.stream().map(DeployerServer::getId).collect(Collectors.toList());
        return Stream.iterate(0, id -> id + 1).limit(MAX_SERVERS).
                filter(i -> !ports.contains(i)).
                findFirst().orElse(-1);
    }

    public int getNextPort()
    {
        List<Integer> ports = deployerServers.stream().map(DeployerServer::getPort).collect(Collectors.toList());
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

    public DeployerServer getServer(int port)
    {
        for(DeployerServer s : deployerServers)
            if(s.getPort() == port)
                return s;
        return null;
    }

    public void clear()
    {
        this.deployerServers.clear();
    }

    public int countLobby(Lobby.LobbyType t)
    {
        int count = 0;
        for(DeployerServer s : deployerServers)
            if(s.getType() == DeployerServer.ServerType.LOBBY && ((Lobby)s).getLobbyType() == t)
                count++;
        return count;
    }
}
