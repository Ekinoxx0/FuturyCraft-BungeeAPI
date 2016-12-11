package api.packets.players;

import api.packets.Packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by loucass003 on 07/12/16.
 */
public class PacketPlayerData extends Packet
{

    protected PacketPlayerData(DataInputStream dis, DataOutputStream dos, Type type)
    {
        super(dis, dos, type);
    }

    @Override
    public void handle()
    {

    }
}
