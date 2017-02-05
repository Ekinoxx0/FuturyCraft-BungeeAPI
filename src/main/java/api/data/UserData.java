package api.data;

import api.Main;
import api.utils.Utils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * Created by SkyBeast on 19/12/2016.
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public final class UserData extends OfflineUserData
{
	@Getter(AccessLevel.PACKAGE)
	private final String redisPrefix;
	@Getter(AccessLevel.PACKAGE)
	private final String base64UUID;
	@Getter
	private final ProxiedPlayer player;


	UserData(ProxiedPlayer player, String base64UUID, String redisPrefix)
	{
		this.player = player;
		this.base64UUID = base64UUID;
		this.redisPrefix = "u:" + base64UUID; // Utils.uuidToBase64(player.getUniqueId());
	}

	public static UserData get(ProxiedPlayer player)
	{
		return Main.getInstance().getDataManager().getData(player);
	}

	@Override
	public boolean isOnline()
	{
		return player.isConnected();
	}

	@Override
	public UserData toOnline()
	{
		return isOnline() ? this : null;
	}

	@Override
	public UUID getUuid()
	{
		return player.getUniqueId();
	}

	@Override
	public int getFuturyCoins()
	{
		return Utils.stringToInt(Utils.returnRedis(jedis -> jedis.get(redisPrefix + ":fc")));
	}

	@Override
	public int getTurfuryCoins()
	{
		return Utils.stringToInt(Utils.returnRedis(jedis -> jedis.get(redisPrefix + ":tc")));
	}

	@Override
	public int getState()
	{
		return Utils.stringToInt(Utils.returnRedis(jedis -> jedis.get(redisPrefix + ":state")));
	}

	@Override
	public int getGroup()
	{
		return Utils.stringToInt(Utils.returnRedis(jedis -> jedis.get(redisPrefix + ":group")));
	}

	@Override
	public void setFuturyCoins(int futuryCoins)
	{
		Utils.doRedis(jedis -> jedis.set(redisPrefix + ":fc", Utils.intToString(futuryCoins)));
	}

	@Override
	public void setTurfuryCoins(int turfuryCoins)
	{
		Utils.doRedis(jedis -> jedis.set(redisPrefix + ":tc", Utils.intToString(turfuryCoins)));
	}

	@Override
	public void setState(int state)
	{
		Utils.doRedis(jedis -> jedis.set(redisPrefix + ":state", Utils.intToString(state)));
	}

	@Override
	public void setGroup(int id)
	{
		Utils.doRedis(jedis -> jedis.set(redisPrefix + ":group", Utils.intToString(id)));
	}
}
