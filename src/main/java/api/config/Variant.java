package api.config;

import java.io.File;

/**
 * Created by loucass003 on 21/12/16.
 */
public class Variant
{
    private int minServers;
    private int maxServers;
    private int slots;
    private int minRam;
    private int maxRam;

    private String spigotArgs;
    private String jvmArgs;
    private File spigotPath;
    private File propsPath;
    private File mapPath;

    public int getMinServers()
    {
        return minServers;
    }

    public int getMaxServers()
    {
        return maxServers;
    }

    public File getMapPath()
    {
        return mapPath;
    }

    public File getPropsPath()
    {
        return propsPath;
    }

    public File getSpigotPath()
    {
        return spigotPath;
    }

    public int getMaxRam()
    {
        return maxRam;
    }

    public int getMinRam()
    {
        return minRam;
    }

    public int getSlots()
    {
        return slots;
    }

    public String getJvmArgs()
    {
        return jvmArgs;
    }

    public String getSpigotArgs()
    {
        return spigotArgs;
    }
}
