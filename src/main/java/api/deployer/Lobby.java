package api.deployer;

import api.Main;
import api.config.DeployerConfig;
import api.config.ServerConfig;
import api.config.ServerTemplate;

import java.io.File;
/**
 * Created by loucass003 on 14/12/16.
 */
public class Lobby extends DeployerServer
{

    private LobbyType type;

    public enum LobbyType
    {
        NORMAL, VIP
    }

    public Lobby(int id, String name, ServerTemplate template, int port)
    {
        super(id, name, ServerType.LOBBY, template, port);
        this.type = getTypeFromName(name);
    }

    public LobbyType getTypeFromName(String name)
    {
        String[] args = name.split("_");
        if(args.length <= 1)
            return null;
        String out = args[1];
        for(int i = 2; i < args.length; i++)
            out += "_" + args[i];
        for(LobbyType l : LobbyType.values())
            if(l.toString().equals(out))
                return l;
        return null;
    }

    @Override
    public void deploy()
    {
        File typeFolder = new File(DeployerConfig.getDeployerDir(), getType().toString());
        File lobbyTypeFolder = new File(typeFolder, getLobbyType().toString());
        this.setServerFolder(new File(lobbyTypeFolder, Integer.toString(getId())));
        super.deploy();
    }

    @Override
    public String getName()
    {
        return "LOBBY " + getLobbyType() + "#" + getId();
    }

    public LobbyType getLobbyType()
    {
        return type;
    }
}
