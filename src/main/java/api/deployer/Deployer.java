package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loucass003 on 14/12/16.
 */
public class Deployer implements Listener
{

    public Main main;
    public List<Server> servers;
    public DeployerConfig config;

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

        for(Lobby.LobbyType t : Lobby.LobbyType.values())
        {
            for(int i = 0; i < t.minCount; i++)
            {
                Server s = new Server(Server.ServerType.LOBBY);
                deploy(s);
            }
        }
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

    public void deploy(Server s)
    {
        this.servers.add(s);
    }

    public void remove(Server s)
    {
        this.servers.remove(s);
    }
}
