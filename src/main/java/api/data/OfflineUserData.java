package api.data;

import api.Main;

import java.util.UUID;

/**
 * Created by SkyBeast on 20/12/2016.
 */
public class OfflineUserData
{
	private UUID uuid;

	OfflineUserData() {}

	private OfflineUserData(UUID uuid)
	{
		this.uuid = uuid;
	}

	public static OfflineUserData get(UUID uuid)
	{
		return new OfflineUserData(uuid);
	}

	public UserData toOnline()
	{
		return Main.getInstance().getDataManager().getOnline(this);
	}

	public boolean isOnline()
	{
		return toOnline() != null;
	}

	public UUID getUUID()
	{
		return uuid;
	}

	@Override
	public String toString()
	{
		return "OfflineUserData{" +
				"uuid=" + uuid +
				'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		OfflineUserData that = (OfflineUserData) o;

		return uuid != null ? uuid.equals(that.uuid) : that.uuid == null;

	}

	@Override
	public int hashCode()
	{
		return uuid != null ? uuid.hashCode() : 0;
	}
}
