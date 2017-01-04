package api.deployer;

import api.Main;
import api.config.Variant;
import net.md_5.bungee.api.config.ServerInfo;

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

    public Lobby(int id, LobbyType type, Variant variant, int port)
    {
        super(id, ServerType.LOBBY, variant, port);
        this.type = type;
	    this.name = "LOBBY-" + getLobbyType() + "#" + getId();
    }

    @Override
    public ServerInfo deploy()
    {
        File typeFolder = new File(Main.getInstance().getDeployer().getConfig().getDeployerDir(), getType().toString());
        File lobbyTypeFolder = new File(typeFolder, getLobbyType().toString());
        this.setServerFolder(new File(lobbyTypeFolder, Integer.toString(getId())));
        return super.deploy();
    }

    public LobbyType getLobbyType()
    {
        return type;
    }
}
