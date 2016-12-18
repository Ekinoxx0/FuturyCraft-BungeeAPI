package api;

import api.deployer.Deployer;
import api.player.PlayerData;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by loucass003 on 06/12/16.
 */
public class Main extends Plugin
{
    public static JedisPool jedisPool;
    public static Main instance;

    public Deployer deployer;

    public Map<UUID, PlayerData> players;


    public Main()
    {
        instance = this;
        this.deployer = new Deployer(this);
        this.players = new HashMap<>();
    }

    @Override
    public void onEnable()
    {
        if(!Main.getInstance().getDataFolder().exists())
            Main.getInstance().getDataFolder().mkdirs();
        jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
        this.deployer.init();
        getLogger().info("FcApiBungee enabled !");
    }

    @Override
    public void onDisable()
    {
        if(!jedisPool.isClosed())
            jedisPool.close();
        jedisPool.destroy();
    }

    public static Main getInstance()
    {
        return instance;
    }
}

