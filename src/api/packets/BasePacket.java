package api.packets;

import api.IPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by loucass003 on 07/12/16.
 */
public class BasePacket implements IPacket {

    private int id;
    private InputStream is;
    private OutputStream os;

    @Override
    public void onReceivePacket(Socket s) throws IOException
    {
        this.is = s.getInputStream();
        this.os = s.getOutputStream();
    }

    public int getId()
    {
        return id;
    }

    public InputStream getInputStream() {
        return is;
    }

    public OutputStream getOutputStream() {
        return os;
    }
}
