package api.deployer;

import api.Main;
import api.config.ServerConfig;
import api.utils.UnzipUtilities;

import java.io.File;
/**
 * Created by loucass003 on 14/12/16.
 */
public class Lobby extends Server
{

    public LobbyType type;
    public int id;
    public int players;


    public enum LobbyType
    {
        NORMAL(3, 100),
        VIP(1, 50);

        int minCount;
        int maxSlots;

        LobbyType(int minCount, int maxSlots)
        {
            this.minCount = minCount;
            this.maxSlots = maxSlots;
        }
    }

    public Lobby(int id, LobbyType type, ServerConfig config, int port)
    {
        super(ServerType.LOBBY, config, port, type.maxSlots);
        this.id = id;
        this.type = type;
    }

    @Override
    public void deploy()
    {
        File typeFolder = new File(Main.getInstance().deployer.deployerFolder, getType().toString());
        File lobbyTypeFolder = new File(typeFolder, getLobbyType().toString());
        serverFolder = new File(lobbyTypeFolder, Integer.toString(getId()));
        super.deploy();
    }

    @Override
    public String getName()
    {
        return "LOBBY " + getLobbyType() + "#" + id;
    }

    public int getId()
    {
        return id;
    }

    public int getPlayers()
    {
        return players;
    }

    public LobbyType getLobbyType()
    {
        return type;
    }
}
