package api;

import api.packets.player.PlayerDataPacket;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by loucass003 on 07/12/16.
 */
public class Server implements Runnable {

    public Main main;
    public ServerSocket server;
    public Thread mainThread;
    public List<String> whiteList;
    public Map<Integer, IPacket> packets;

    public Server(Main main)
    {
        this.main = main;
        this.whiteList = new ArrayList<>();
        this.packets = new HashMap<>();
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

    public void registerPackets()
    {
        this.packets.put(0, new PlayerDataPacket());
    }

    @Override
    public void run()
    {
        while(!server.isClosed())
        {
            try {
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

                InputStream is = client.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                String json = sb.toString();
                br.close();
                JsonElement jelement = new JsonParser().parse(json);
                JsonObject jobject = jelement.getAsJsonObject();
                int id = jobject.getAsJsonObject("id").getAsInt();
                if(!packets.containsKey(id))
                {
                    //TODO : send error;
                    client.close();
                    continue;
                }

                IPacket p = new Gson().fromJson(json, packets.get(id).getClass());
                p.onReceivePacket(client);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
