package api.data;

import api.Main;
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
}
