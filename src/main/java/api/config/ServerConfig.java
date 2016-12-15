package api.config;

/**
 * Created by loucass003 on 15/12/16.
 */
public class ServerConfig
{

    public String type;
    public String spigotPath;
    public String mapPath;
    public String propsPath;

    public String getMapPath()
    {
        return mapPath;
    }

    public String getPropsPath()
    {
        return propsPath;
    }

    public String getSpigotPath()
    {
        return spigotPath;
    }

    public String getType()
    {
        return type;
    }

    public void setMapPath(String mapPath)
    {
        this.mapPath = mapPath;
    }

    public void setPropsPath(String propsPath)
    {
        this.propsPath = propsPath;
    }

    public void setSpigotPath(String spigotPath)
    {
        this.spigotPath = spigotPath;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
