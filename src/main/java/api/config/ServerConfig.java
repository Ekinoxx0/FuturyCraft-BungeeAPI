package api.config;

import api.deployer.Deployer;
import api.deployer.DeployerServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by loucass003 on 19/12/16.
 */
public class ServerConfig
{
    private String name;
    private DeployerServer.ServerType type;

    private List<ServerTemplate> templates;

    public ServerConfig(String name, Map<String, Object> fields)
    {
        this.name = name;
        this.type = this.getTypeByName((String) fields.get("type"));

        this.templates = new ArrayList<>();
        if(fields.containsKey("templates"))
        {
            List<Map<String, Object>> maptemplates = (List<Map<String, Object>>) fields.get("templates");
            maptemplates.forEach(item -> templates.add(new ServerTemplate(item)));
        }
    }

    public DeployerServer.ServerType getTypeByName(String type)
    {
        for(DeployerServer.ServerType t : DeployerServer.ServerType.values())
            if(t.toString().equals(type))
                return t;
        return null;
    }

    public List<ServerTemplate> getTemplates()
    {
        return templates;
    }

    public String getName()
    {
        return name;
    }

    public DeployerServer.ServerType getType()
    {
        return type;
    }
}
