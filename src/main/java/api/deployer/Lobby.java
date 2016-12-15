package api.deployer;

/**
 * Created by loucass003 on 14/12/16.
 */
public class Lobby extends Server
{

    public LobbyType type;
    public int id;
    public int players;

    public enum LobbyType
    {
        NORMAL(3, 100),
        VIP(1, 50);

        int minCount;
        int maxSlots;

        LobbyType(int minCount, int maxSlots)
        {
            this.minCount = minCount;
            this.maxSlots = maxSlots;
        }
    }

    public Lobby(int id, LobbyType type)
    {
        super(ServerType.LOBBY);
        this.id = id;
        this.type = type;
    }

    public int getId()
    {
        return id;
    }

    public int getPlayers()
    {
        return players;
    }

    public LobbyType getLobbyType()
    {
        return type;
    }
}
