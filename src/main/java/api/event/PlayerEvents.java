package api.event;

import api.Main;
import api.config.Template;
import api.config.Variant;
import api.data.Server;
import api.deployer.Deployer;
import api.deployer.DeployerServer;
import api.deployer.Lobby;
import api.utils.SimpleManager;
import lombok.ToString;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

/**
 * Created by loucass003 on 21/12/16.
 */
@ToString(exclude = {"deployer"})
public final class PlayerEvents implements Listener, SimpleManager
{
	private final Deployer deployer = Main.getInstance().getDeployer();
	private boolean init;

	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialised!");

		Main.getInstance().getProxy().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@EventHandler
	public void onPostLogin(ServerConnectEvent e)
	{
		List<Server> lobbies = Main.getInstance().getDataManager().getServersByType(DeployerServer.ServerType.LOBBY);
		if (lobbies.isEmpty())
		{
			e.getPlayer().disconnect(new TextComponent("Server is starting !"));
			return;
		}

		lobbies.sort((o1, o2) ->
			{
				int i0 = o1.getInfo().getPlayers().size();
				int i1 = o2.getInfo().getPlayers().size();
				return (i0 > i1) ? 1 : ((i0 < i1) ? -1 : 0);
			}
		);

		ServerInfo i = lobbies.get(0).getInfo();
		if(!e.getTarget().equals(e.getPlayer().getServer()))
			e.setTarget(i);

		int players = Main.getInstance().getProxy().getOnlineCount();
		int totalSlots = 0;
		for (Server s : lobbies)
			totalSlots += s.getDeployer().getVariant().getSlots();

		int average = (players / totalSlots);

		if (average > 0.7)
		{
			int id = deployer.getNextId();
			int port = deployer.getNextPort();
			Variant v = getNextLobbyVariant(Lobby.LobbyType.NORMAL);
			DeployerServer server = new Lobby(id, Lobby.LobbyType.NORMAL, v, port);
			deployer.addServer(server);
		}
	}


	private Variant getNextLobbyVariant(Lobby.LobbyType type)
	{
		List<Template.LobbyTemplate> lobbies = deployer.getConfig().getLobbiesByType(type);
		if (lobbies.isEmpty())
			return null;
		Template.LobbyTemplate t = lobbies.stream().findFirst().orElse(null);
		if (t == null)
			return null;
		if (lobbies.size() == 1)
			return t.getVariants().get(0);
		t.setOffset(t.getOffset() + 1);
		if (t.getOffset() >= t.getVariants().size())
			t.setOffset(0);
		return t.getVariants().get(t.getOffset());
	}
}
