package api.pakets.players;

import api.packets.Packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by loucass003 on 07/12/16.
 */
public class PacketPlayerData extends Packet {

    protected PacketPlayerData(DataInputStream dis, DataOutputStream dos)
    {
        super(dis, dos);
    }

    @Override
    public void handle() {

    }
}
