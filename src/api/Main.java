package api;

import net.md_5.bungee.api.plugin.Plugin;

/**
 * Created by loucass003 on 06/12/16.
 */
public class Main extends Plugin{

    public ServerSocket serverSocket;

    public Main()
    {
        this.serverSocket = new ServerSocket(this);
    }

    @Override
    public void onEnable() {
        this.serverSocket.init();
        getLogger().info("FcApiBungee enabled !");
    }

}
