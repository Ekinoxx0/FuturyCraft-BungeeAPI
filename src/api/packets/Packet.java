package api.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by loucass003 on 07/12/16.
 */
public class Packet
{
    protected DataInputStream dis;
    protected DataOutputStream dos;

    protected Packet(DataInputStream dis, DataOutputStream dos)
    {
        this.dis = dis;
        this.dos = dos;
    }

    public void handle() {}
}
