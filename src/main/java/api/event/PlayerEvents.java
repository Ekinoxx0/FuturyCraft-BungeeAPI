package api.event;

import api.Main;
import api.data.Server;
import api.deployer.DeployerServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by loucass003 on 21/12/16.
 */
public class PlayerEvents implements Listener
{
    public PlayerEvents()
    {
        Main.getInstance().getProxy().getPluginManager().registerListener(Main.getInstance(), this);
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e)
    {
        List<Server> lobbies = Main.getInstance().getDataManager().getServersByType(DeployerServer.ServerType.LOBBY);
        if(lobbies.size() == 0)
        {
            e.getPlayer().disconnect(new TextComponent("Server is starting !"));
            return;
        }

        Collections.sort(lobbies, (o1, o2) ->
        {
            int i0 = o1.getDeployer().getInfo().getPlayers().size();
            int i1 = o2.getDeployer().getInfo().getPlayers().size();
            return i0 > i1 ? 1 : i0 < i1 ? -1 : 0;
        });

        if(lobbies.get(0) != null) //maybe not necessary
        {
            e.getPlayer().connect(lobbies.get(0).getInfo());
        }
    }
}
