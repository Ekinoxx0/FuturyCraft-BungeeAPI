package api.config;

/**
 * Created by loucass003 on 15/12/16.
 */
public class ServerConfig
{
    private String spigotPath;
    private String mapPath;
    private String propsPath;
    private String jvmArgs;
    private String spigotArgs;
    private int minRam;
    private int maxRam;

    public ServerConfig(String spigotPath, String mapPath, String propsPath, String jvmArgs, int minRam, int maxRam) {
        this.spigotPath = spigotPath;
        this.mapPath = mapPath;
        this.propsPath = propsPath;
        this.jvmArgs = jvmArgs;
        this.minRam = minRam;
        this.maxRam = maxRam;
    }

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

    public String getJvmArgs() {
        return jvmArgs;
    }

    public String getSpigotArgs() {
        return spigotArgs;
    }

    public int getMinRam() {
        return minRam;
    }

    public int getMaxRam() {
        return maxRam;
    }
}