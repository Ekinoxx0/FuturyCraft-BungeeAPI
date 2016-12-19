package api.config;

import api.Main;
import api.deployer.Server;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by loucass003 on 15/12/16.
 */
public class DeployerConfig
{

    public Map<Server.ServerType, Map<String, ServerConfig>> serversTemplates;

    public DeployerConfig()
    {
        this.serversTemplates = new HashMap<>();
    }

    public Map<Server.ServerType, Map<String, ServerConfig>> getServersTemplates()
    {
        return serversTemplates;
    }

    public static DeployerConfig getConfig()
    {

        File f = new File(Main.getInstance().getDataFolder(), "deployer.json");
        if(!f.exists())
        {
            try {
                if(!f.createNewFile())
                {
                    Main.getInstance().getLogger().info("Unable to create deployer config file");
                    return null;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        return new Gson().fromJson(Utils.readFile(f), DeployerConfig.class);
    }
}
