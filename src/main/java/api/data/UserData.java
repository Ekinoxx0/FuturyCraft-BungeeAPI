package api.data;

import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Created by SkyBeast on 19/12/2016.
 */
public class UserData
{
	private final ProxiedPlayer player;
	private final String redisPrefix;

	UserData(ProxiedPlayer player)
	{
		this.player = player;
		this.redisPrefix = DataManager.uuidToBase64(player.getUniqueId());
	}

	public ProxiedPlayer getPlayer()
	{
		return player;
	}

	String getRedisPrefix()
	{
		return redisPrefix;
	}
}
