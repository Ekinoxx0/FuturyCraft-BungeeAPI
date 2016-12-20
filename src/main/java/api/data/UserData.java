package api.data;

import api.Main;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by SkyBeast on 19/12/2016.
 */
public final class UserData extends OfflineUserData
{
	private ProxiedPlayer player;
	private final String redisPrefix;
	private final Delay delay = new Delay();

	private UserData(ProxiedPlayer player)
	{
		this.player = player;
		this.redisPrefix = DataManager.uuidToBase64(player.getUniqueId());
	}

	public static UserData get(ProxiedPlayer player)
	{
		return Main.getInstance().getDataManager().getData(player);
	}

	public void setPlayer(ProxiedPlayer player)
	{
		this.player = player;
	}

	public ProxiedPlayer getPlayer()
	{
		return player;
	}

	String getRedisPrefix()
	{
		return redisPrefix;
	}

	Delay getDelayer()
	{
		return delay;
	}

	@Override
	public boolean isOnline()
	{
		return player.isConnected();
	}

	@Override
	public UserData toOnline()
	{
		return this;
	}

	@Override
	public UUID getUUID()
	{
		return player.getUniqueId();
	}

	@Override
	public String toString()
	{
		return "UserData{" +
				"player=" + player +
				", redisPrefix='" + redisPrefix + '\'' +
				", delay=" + delay +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UserData userData = (UserData) o;

		return player != null ? player.equals(userData.player) : userData.player == null && (redisPrefix != null ?
				redisPrefix.equals(userData.redisPrefix) : userData.redisPrefix == null && (delay != null ? delay
				.equals(userData.delay) : userData.delay == null));

	}

	@Override
	public int hashCode()
	{
		int result = player != null ? player.hashCode() : 0;
		result = 31 * result + (redisPrefix != null ? redisPrefix.hashCode() : 0);
		result = 31 * result + (delay != null ? delay.hashCode() : 0);
		return result;
	}

	class Delay implements Delayed
	{
		private long reach;

		long getReach()
		{
			return reach;
		}

		void setReach(long reach)
		{
			this.reach = reach;
		}

		UserData parent()
		{
			return UserData.this;
		}

		@Override
		public String toString()
		{
			return "Delay{" +
					"reach=" + reach +
					"getDelay(TimeUnit.MILLISECONDS)=" + getDelay(TimeUnit.MILLISECONDS) +
					'}';
		}

		@Override
		public long getDelay(TimeUnit unit)
		{
			return unit.convert(reach - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed o)
		{
			return (reach == ((Delay) o).reach ? 0 : (reach < ((Delay) o).reach ? -1 : 1));
		}
	}
}
