package api.deployer;

import api.config.Variant;
import api.data.Server;
import lombok.*;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Created by loucass003 on 15/12/16.
 */
@ToString(exclude = {"server"})
public class DeployerServer
{
	@Getter
	protected String id;
	@Getter
	protected String name;
	@Getter
	protected final Variant variant;
	@Getter
	protected final ServerType type;
	@Getter
	protected final int port;
	@Setter(AccessLevel.PACKAGE)
	protected Server server;
	protected ServerInfo info;

	public DeployerServer(ServerType type, Variant variant, int port)
	{
		this.type = type;
		this.variant = variant;
		this.port = port;
	}

	public ServerInfo deploy()
	{

		return info;
	}

	public void kill()
	{

	}

	public String getConsole()
	{
		return "Internal Error :(";
	}

	public void sendCommand(String command)
	{

	}

	@Getter
	@AllArgsConstructor
	public enum ServerType
	{
		LOBBY("Lobby"),
		GAME("Game");

		private final String name;

		@Override
		public String toString()
		{
			return name;
		}
	}
}