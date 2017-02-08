package api.data;

import api.Main;
import api.perms.Group;
import api.utils.Utils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;


/**
 * Created by loucass003 on 2/7/17.
 */
@Data
public class UserData
{
	private final MongoUser mongoImpl;
	private final RedisUser redisImpl;

	@Getter(AccessLevel.PACKAGE) private final String redisPrefix;
	@Getter(AccessLevel.PACKAGE) private final String base64UUID;
	@Getter private final ProxiedPlayer player;

	UserData(ProxiedPlayer player, String base64UUID)
	{
		this.player = player;
		this.base64UUID = base64UUID;
		redisPrefix = "u:" + base64UUID;
		mongoImpl = new MongoUser(base64UUID, redisPrefix);
		redisImpl = new RedisUser(redisPrefix);
	}

	private boolean cached()
	{
		return Main.getInstance().getDataManager().userCached(this);
	}

	public boolean isOnline()
	{
		return player.isConnected();
	}

	public UUID getUuid()
	{
		return Utils.uuidFromBase64(base64UUID);
	}

	/*-----------------*
	 *      IDATA      *
	 *-----------------*/

	public int getFuturyCoins()
	{
		return cached() ? redisImpl.getFuturyCoins() : mongoImpl.getFuturyCoins();
	}

	public void setFuturyCoins(int fc, boolean forced)
	{
		if(forced)
		{
			redisImpl.setFuturyCoins(fc);
			mongoImpl.setFuturyCoins(fc);
		}
		else if (cached())
			redisImpl.setFuturyCoins(fc);
		else
			mongoImpl.setFuturyCoins(fc);
	}

	public int getTurfuryCoins()
	{
		return cached() ? redisImpl.getTurfuryCoins() : mongoImpl.getTurfuryCoins();
	}

	public void setTurfuryCoins(int tc, boolean forced)
	{
		if(forced)
		{
			redisImpl.setTurfuryCoins(tc);
			mongoImpl.setTurfuryCoins(tc);
		}
		else if (cached())
			redisImpl.setTurfuryCoins(tc);
		else
			mongoImpl.setTurfuryCoins(tc);
	}

	public Group getGroup()
	{
		return cached() ? redisImpl.getGroup() : redisImpl.getGroup();
	}

	public void setGroup(Group g, boolean forced)
	{
		if(forced)
		{
			redisImpl.setGroup(g);
			mongoImpl.setGroup(g);
		}
		else if (cached())
			redisImpl.setGroup(g);
		else
			mongoImpl.setGroup(g);
	}

}
