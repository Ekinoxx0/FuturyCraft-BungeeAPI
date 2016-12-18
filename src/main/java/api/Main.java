package api;

import api.config.DeployerConfig;
import api.deployer.Deployer;
import api.packets.PacketServer;
import api.player.PlayerData;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Created by loucass003 on 06/12/16.
 */
public class Main extends Plugin
{
    public static JedisPool jedisPool;
    public static Main instance;

    public PacketServer serverSocket;
    public Deployer deployer;

    public Map<UUID, PlayerData> players;


    public Main()
    {
        instance = this;
        this.serverSocket = new PacketServer(this);
        this.deployer = new Deployer(this);
        this.players = new HashMap<>();
    }

    @Override
    public void onEnable()
    {
        if(!Main.getInstance().getDataFolder().exists())
            Main.getInstance().getDataFolder().mkdirs();
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
        this.deployer.init();
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

    public static Main getInstance()
    {
        return instance;
    }
}

