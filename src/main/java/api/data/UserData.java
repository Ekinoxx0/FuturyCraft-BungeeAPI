package api.data;

import api.Main;
import lombok.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

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
	@Getter(AccessLevel.PACKAGE)
	private final Delay delayer = new Delay();
	@Getter @Setter(AccessLevel.PACKAGE)
	private ProxiedPlayer player;

	UserData(ProxiedPlayer player, String base64UUID)
	{
		this.player = player;
		this.base64UUID = base64UUID;
		redisPrefix = "u:" + base64UUID; // Utils.uuidToBase64(player.getUniqueId());
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

	class Delay implements Delayed
	{
		long deadLine;

		UserData parent()
		{
			return UserData.this;
		}

		@Override
		public String toString()
		{
			return "Delay{" +
					"deadLine=" + deadLine +
					"getDelay(TimeUnit.MILLISECONDS)=" + getDelay(TimeUnit.MILLISECONDS) +
					'}';
		}

		@Override
		public long getDelay(TimeUnit unit)
		{
			return unit.convert(deadLine - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed o)
		{
			return (deadLine == ((Delay) o).deadLine ? 0 : (deadLine < ((Delay) o).deadLine ? -1 : 1));
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof Delay && ((Delay) o).deadLine == deadLine;
		}

		@Override
		public int hashCode()
		{
			return (int) (deadLine ^ (deadLine >>> 32));
		}
	}
}
