package api;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

/**
 * Created by loucass003 on 06/12/16.
 */
public class Main extends Plugin
{

    public Server serverSocket;

    public Main()
    {
        this.serverSocket = new Server(this);
    }

    @Override
    public void onEnable() {
        try {
            this.serverSocket.init();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("[FcAPI] Unable to start socket server");
        }
        getLogger().info("FcApiBungee enabled !");
    }

}
