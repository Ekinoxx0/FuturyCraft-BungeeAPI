package api;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by loucass003 on 07/12/16.
 */
public interface IPacket {

    public void onReceivePacket(Socket s) throws IOException;
}