package api.event;

import api.Main;
import api.config.Template;
import api.config.Variant;
import api.data.Server;
import api.deployer.Deployer;
import api.deployer.DeployerServer;
import api.deployer.Lobby;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Collections;
import java.util.List;

/**
 * Created by loucass003 on 21/12/16.
 */
public class PlayerEvents implements Listener
{
	private final Deployer deployer = Main.getInstance().getDeployer();

	public void init()
	{
		Main.getInstance().getProxy().getPluginManager().registerListener(Main.getInstance(), this);
	}

	@EventHandler
	public void onPlayerJoin(PostLoginEvent e)
	{
		List<Server> lobbies = Main.getInstance().getDataManager().getServersByType(DeployerServer.ServerType.LOBBY);
		if (lobbies.size() == 0)
		{
			e.getPlayer().disconnect(new TextComponent("Server is starting !"));
			return;
		}

		Collections.sort(lobbies, (o1, o2) ->
		{
			int i0 = o1.getInfo().getPlayers().size();
			int i1 = o2.getInfo().getPlayers().size();
			return i0 > i1 ? 1 : i0 < i1 ? -1 : 0;
		});

		e.getPlayer().connect(lobbies.get(0).getInfo());

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


	public Variant getNextLobbyVariant(Lobby.LobbyType type)
	{
		List<Template.LobbyTemplate> lobbies = deployer.getConfig().getLobbiesByType(type);
		if (lobbies.size() == 0)
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