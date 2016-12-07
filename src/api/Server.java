package api;

import api.packets.Packet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by loucass003 on 07/12/16.
 */
public class Server implements Runnable {

    public Main main;
    public ServerSocket server;
    public Thread mainThread;
    public List<String> whiteList;

    public Server(Main main)
    {
        this.main = main;
        this.whiteList = new ArrayList<>();
    }

    public void init() throws IOException
    {
        this.whiteList.add("localhost");
        this.whiteList.add("127.0.0.1");

        this.server = new ServerSocket(25564);
        this.mainThread = new Thread(this);
        this.mainThread.start();
    }

    public synchronized void clear() throws IOException
    {
        this.server.close();
        this.whiteList.clear();
        this.mainThread.interrupt();
    }

    @Override
    public void run()
    {
        while(!server.isClosed())
        {
            try
            {
                Socket client = this.server.accept();

                boolean accept = false;
                for (String addr : whiteList)
                {
                    if (client.getRemoteSocketAddress().toString().equals(addr))
                    {
                        accept = true;
                        break;
                    }
                }

                if(!accept)
                {
                    client.close();
                    continue;
                }

                DataInputStream dis = new DataInputStream(client.getInputStream());
                DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                Packet p = PacketTypes.getNewPacket(dis, dos);
                if(p == null)
                {
                    //TODO : send error;
                    client.close();
                    continue;
                }

                p.handle();
            }
            catch (IOException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }
    }
}
