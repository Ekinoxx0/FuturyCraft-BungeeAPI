package api.config;

import api.deployer.Lobby;

import java.util.List;

/**
 * Created by loucass003 on 21/12/16.
 */
public class Template
{

    public class LobbyTemplate extends Template
    {
        private Lobby.LobbyType type;

        public Lobby.LobbyType getType()
        {
            return type;
        }
    }

    private List<Variant> variants;
    private String displayName;
    private transient int offset;

    public List<Variant> getVariants()
    {
        return variants;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }
}
