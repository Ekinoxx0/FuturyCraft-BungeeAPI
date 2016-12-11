package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Modifier;

/**
 * Created by loucass003 on 10/12/16.
 */
public class Model {

    protected final String list;
    protected final String key;

    public Model(String list, String key)
    {
        this.list = list;
        this.key = key;
    }

    public String toString()
    {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(Modifier.PROTECTED);
        Gson gson = builder.create();
        return gson.toJson(this);
    }

    public void save()
    {
        Jedis jedis = null;
        try {
            jedis = Main.jedisPool.getResource();
            jedis.hset(list, key, this.toString());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
