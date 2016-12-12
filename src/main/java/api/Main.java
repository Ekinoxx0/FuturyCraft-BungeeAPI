package api;

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

    public Server serverSocket;
    public static JedisPool jedisPool;
    public Map<UUID, PlayerData> players;

    public Main()
    {
        this.serverSocket = new Server(this);
        this.players = new HashMap<>();
    }

    @Override
    public void onEnable()
    {
        jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
        try
        {
            this.serverSocket.init();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            getLogger().severe("[FcAPI] Unable to start socket server");
        }
        getLogger().info("FcApiBungee enabled !");
    }

    @Override
    public void onDisable()
    {
        if(!jedisPool.isClosed())
            jedisPool.close();
        jedisPool.destroy();
        try
        {
            this.serverSocket.clear();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            getLogger().severe("[FcAPI] Unable to close socket server");
        }
    }

}
