package api.deployer;

import api.Main;
import api.config.Variant;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.File;

/**
 * Created by loucass003 on 14/12/16.
 */
public class Lobby extends DeployerServer
{
	private final LobbyType type;
	@Getter
	private int acceptedPlayers;
	@Getter
	@Setter
	private long stopAcceptPlayerTimestamp;

	public Lobby(int id, LobbyType type, Variant variant, int port)
	{
		super(id, ServerType.LOBBY, variant, port);
		this.type = type;
		name = "LOBBY-" + type + '#' + base64UUID;
	}

	@Override
	public ServerInfo deploy()
	{
		File typeFolder = new File(Main.getInstance().getDeployer().getConfig().getDeployerDir(),
				getType().toString());
		File lobbyTypeFolder = new File(typeFolder, getLobbyType().toString());
		serverFolder = new File(lobbyTypeFolder, Integer.toString(getOffset()));
		return super.deploy();
	}

	public void incrementAcceptedPlayers()
	{
		acceptedPlayers++;
	}

	public LobbyType getLobbyType()
	{
		return type;
	}

	public enum LobbyType
	{
		NORMAL("Normal"), VIP("VIP");

		private final String name;

		LobbyType(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
