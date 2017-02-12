package api.config;

import api.lobby.LobbyType;
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
		private final LobbyType type;

		public LobbyTemplate(List<Variant> variants, String displayName, LobbyType type)
		{
			super(variants, displayName);
			this.type = type;
		}
	}
}
