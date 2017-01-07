package api.config;

import api.deployer.Lobby;

import java.util.List;

/**
 * Created by loucass003 on 21/12/16.
 */
public class Template
{

	private List<Variant> variants;
	private String displayName;
	private transient int offset;

	public List<Variant> getVariants()
	{
		return variants;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	@Override
	public String toString()
	{
		return "Template{" +
				"variants=" + variants +
				", displayName='" + displayName + '\'' +
				", offset=" + offset +
				'}';
	}

	public static class LobbyTemplate extends Template
	{
		private Lobby.LobbyType type;

		public Lobby.LobbyType getType()
		{
			return type;
		}

		@Override
		public String toString()
		{
			return "LobbyTemplate{" +
					"variants=" + super.variants +
					", displayName='" + super.displayName + '\'' +
					", offset=" + super.offset +
					", type=" + type +
					'}';
		}
	}
}
