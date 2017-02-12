package api.deployer;

import api.Main;
import api.config.Variant;
import lombok.Getter;
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

	public Lobby(LobbyType type, Variant variant, int port)
	{
		super(ServerType.LOBBY, variant, port);
		this.type = type;
		name = "LOBBY-" + type + '#' + base64UUID;
	}

	@Override
	public ServerInfo deploy()
	{
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
