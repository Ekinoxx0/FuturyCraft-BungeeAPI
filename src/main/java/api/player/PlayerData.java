package api.player;

import api.Model;

import java.util.UUID;

/**
 * Created by loucass003 on 09/12/16.
 */
public class PlayerData extends Model
{
    private String uuid;
    private String name;
    private double futuryCoins;
    private double turfuryCoins;
    private double reputation;

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

    public void setFuturyCoins(double futuryCoins)
    {
        this.futuryCoins = futuryCoins;
    }

    public void setReputation(double reputation)
    {
        this.reputation = reputation;
    }

    public void setTurfuryCoins(double turfuryCoins)
    {
        this.turfuryCoins = turfuryCoins;
    }

    public String getUuid()
    {
        return uuid;
    }

    public double getFuturyCoins()
    {
        return futuryCoins;
    }

    public double getReputation()
    {
        return reputation;
    }

    public double getTurfuryCoins()
    {
        return turfuryCoins;
    }

    public String getName()
    {
        return name;
    }
}
