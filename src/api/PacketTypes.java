package api;

import api.packets.Packet;
import api.packets.players.PacketPlayerData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by loucass003 on 07/12/16.
 */

public enum PacketTypes
{

    PLAYERDATA(0x0, PacketPlayerData.class);

    public int id;
    public Class<? extends Packet> clazz;

    PacketTypes(int id, Class<? extends Packet> clazz)
    {
        this.id = id;
        this.clazz = clazz;
    }

    public static Packet getNewPacket(DataInputStream dis, DataOutputStream dos)
            throws IOException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException
    {
        int id = dis.readByte() & 0xf;
        for (PacketTypes p : values())
            if (id == p.id)
                return p.clazz.getDeclaredConstructor(DataInputStream.class, DataInputStream.class).newInstance(dis, dos);
        return null;
    }
}
