package api.data;

import api.Main;
import lombok.Data;

import java.util.UUID;

/**
 * Created by SkyBeast on 20/12/2016.
 */
@Data
public class OfflineUserData
{
	private final UUID uuid;

	OfflineUserData() {uuid = null;}

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
}
