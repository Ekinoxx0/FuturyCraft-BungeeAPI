package api.deployer;

import java.io.File;

/**
 * Created by loucass003 on 15/12/16.
 */
public class Server
{

    public String name;
    public ServerType type;
    public File template;

    public enum ServerType
    {
        LOBBY,
        GAME
    }

    public Server(ServerType type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public File getTemplate()
    {
        return template;
    }

    public ServerType getType()
    {
        return type;
    }
}
