package api;

import java.util.UUID;

/**
 * Created by loucass003 on 09/12/16.
 */
public class PlayerData extends Model
{
    private String uuid;
    private String name;
    private int futuryCoins;
    private int turfuryCoins;
    private int reputation;

    public PlayerData(String uuid)
    {
        super("players", uuid);
        this.uuid = uuid;
    }

    public PlayerData(UUID uuid)
    {
        this(uuid.toString());
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setFuturyCoins(int futuryCoins)
    {
        this.futuryCoins = futuryCoins;
    }

    public void setReputation(int reputation)
    {
        this.reputation = reputation;
    }

    public void setTurfuryCoins(int turfuryCoins)
    {
        this.turfuryCoins = turfuryCoins;
    }

    public String getUuid()
    {
        return uuid;
    }

    public int getFuturyCoins()
    {
        return futuryCoins;
    }

    public int getReputation()
    {
        return reputation;
    }

    public int getTurfuryCoins()
    {
        return turfuryCoins;
    }

    public String getName()
    {
        return name;
    }
}
