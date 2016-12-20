package api.config;

import java.util.Map;

/**
 * Created by loucass003 on 19/12/16.
 */
public class ServerTemplate
{

    private String spigotArgs;
    private String jvmArgs;

    private String mapPath;
    private String spigotPath;
    private String propsPath;

    private int minRam;
    private int maxRam;
    private int minServers;
    private int maxServers;
    public int slots;

    private Map<String, Object> fields;

    public ServerTemplate(Map<String, Object> fields)
    {
        this.fields = fields;
        this.spigotArgs = (String) fields.get("spigotArgs");
        this.jvmArgs = (String) fields.get("jvmArgs");
        this.mapPath = (String) fields.get("mapPath");
        this.spigotPath = (String) fields.get("spigotPath");
        this.propsPath = (String) fields.get("propsPath");
        this.minRam = (Integer) fields.get("minRam");
        this.maxRam = (Integer) fields.get("maxRam");
        this.minServers = (Integer) fields.get("minServers");
        this.maxServers = (Integer) fields.get("maxServers");
        this.slots = (Integer) fields.get("slots");
    }

    public String getJvmArgs()
    {
        return jvmArgs;
    }

    public int getMaxRam()
    {
        return maxRam;
    }

    public int getSlots()
    {
        return slots;
    }

    public int getMinRam()
    {
        return minRam;
    }

    public Map<String, Object> getFields()
    {
        return fields;
    }

    public String getMapPath()
    {
        return mapPath;
    }

    public String getPropsPath()
    {
        return propsPath;
    }

    public String getSpigotArgs()
    {
        return spigotArgs;
    }

    public String getSpigotPath()
    {
        return spigotPath;
    }

    public int getMaxServers()
    {
        return maxServers;
    }

    public int getMinServers()
    {
        return minServers;
    }

}
