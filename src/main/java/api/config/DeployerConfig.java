package api.config;

import api.utils.Utils;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.util.Map;

/**
 * Created by loucass003 on 19/12/16.
 */
public class DeployerConfig
{
    private Map<String, Map<String, Object>> servers;
    private static File deployerDir;
    private static File baseDir;
    public void load(File config)
    {
        Yaml yaml = new Yaml();
        Object o = yaml.load(Utils.readFile(config));
        if(o instanceof Map<?, ?>)
        {
            Map<?, ?> map = (Map<?, ?>)o;
            if (map.containsKey("servers"))
                this.servers = (Map<String, Map<String, Object>>) map.get("servers");
            if(map.containsKey("baseDir"))
            {
                Object obj = map.get("baseDir");
                if(obj instanceof String)
                    baseDir = new File((String)obj);
            }
            if(map.containsKey("deployerDir"))
            {
                Object obj = map.get("deployerDir");
                if(obj instanceof String)
                    deployerDir = new File(baseDir, (String)obj);
            }
        }
    }
    public static File getBaseDir()
    {
        return baseDir;
    }
    public static File getDeployerDir()
    {
        return deployerDir;
    }
    public Map<String, Map<String, Object>> getServers()
    {
        return servers;
    }
}
