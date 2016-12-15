package api.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by loucass003 on 07/12/16.
 */
public class Packet
{
    public enum Type {
        GETTER,
        SETTER;
    }

    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected Type type;

    protected Packet(DataInputStream dis, DataOutputStream dos, Type type)
    {
        this.dis = dis;
        this.dos = dos;
        this.type = type;
    }

    public void handle() {}
}
