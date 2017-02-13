package api.data;

import api.Main;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.NotSaved;

import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by SkyBeast on 12/02/17.
 */
@Entity
@NoArgsConstructor
public class UserData
{
	@Getter
	@Id
	private UUID uniqueID;
	@Getter
	@NotSaved
	private long firstJoin;
	@Getter
	private long futuryCoins;
	@Getter
	private long turfuryCoins;
	private transient Delayer delayer;

	public UserData(UUID uniqueID)
	{
		this.uniqueID = uniqueID;
	}

	public static UserData get(ProxiedPlayer player)
	{
		return Main.getInstance().getUserDataManager().getData(player);
	}

	public static UserData get(UUID player)
	{
		return Main.getInstance().getUserDataManager().getData(player);
	}

	Delayer getDelayer()
	{
		if (delayer == null)
			delayer = new Delayer();
		return delayer;
	}

	public ProxiedPlayer getBungee()
	{
		return ProxyServer.getInstance().getPlayer(uniqueID);
	}


	@Data
	class Delayer implements Delayed
	{
		private long deadLine;

		UserData parent()
		{
			return UserData.this;
		}

		@Override
		public String toString()
		{
			return "Delayer{" +
					"deadLine=" + deadLine +
					", getDelay(TimeUnit.MILLISECONDS)=" + getDelay(TimeUnit.MILLISECONDS) +
					'}';
		}

		@Override
		public long getDelay(TimeUnit unit) //Negative when deadLine passed
		{
			return unit.convert(deadLine - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed o) //Older first
		{
			return (deadLine == ((Delayer) o).deadLine ? 0 : (deadLine > ((Delayer) o).deadLine ? -1 : 1));
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof Delayer && ((Delayer) o).deadLine == deadLine;
		}

		@Override
		public int hashCode()
		{
			return (int) (deadLine ^ (deadLine >>> 32));
		}
	}
}
