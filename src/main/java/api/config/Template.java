package api.config;

import api.deployer.Lobby;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Created by loucass003 on 21/12/16.
 */
@Data
public class Template
{
	private final List<Variant> variants;
	private final String displayName;
	private transient int offset;

	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class LobbyTemplate extends Template
	{
		private final Lobby.LobbyType type;
	}
}
