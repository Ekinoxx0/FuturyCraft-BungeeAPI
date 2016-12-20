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
	private final String base64UUID;
	private final Delay delay = new Delay();

	UserData(ProxiedPlayer player, String base64UUID)
	{
		this.player = player;
		this.base64UUID = base64UUID;
		this.redisPrefix = "u:" + base64UUID; // Utils.uuidToBase64(player.getUniqueId());
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

	String getBase64UUID()
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
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		UserData userData = (UserData) o;

		return player != null ? player.equals(userData.player) : userData.player == null && redisPrefix.equals
				(userData.redisPrefix) && (base64UUID != null ? base64UUID.equals(userData.base64UUID) : userData
				.base64UUID == null && delay.equals(userData.delay));

	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (player != null ? player.hashCode() : 0);
		result = 31 * result + redisPrefix.hashCode();
		result = 31 * result + (base64UUID != null ? base64UUID.hashCode() : 0);
		result = 31 * result + delay.hashCode();
		return result;
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
	}

	@Override
	public String toString()
	{
		return "UserData{" +
				"player=" + player +
				", redisPrefix='" + redisPrefix + '\'' +
				", base64UUID='" + base64UUID + '\'' +
				", delay=" + delay +
				'}';
	}
}
