package api.data;

import api.Main;
import api.utils.Utils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

/**
 * Created by SkyBeast on 20/12/2016.
 */
@Data
public class OfflineUserData
{
	private static final Document EMPTY_DOCUMENT = new Document();
	private static final MongoDatabase DB = Main.getInstance().getMongoClient().getDatabase("users"); //Does not
	// open any connection
	private final UUID uuid;
	@Getter(AccessLevel.PACKAGE)
	private final String base64UUID;
	@Getter(AccessLevel.NONE)
	private final MongoCollection<Document> collection;

	OfflineUserData()
	{
		uuid = null;
		base64UUID = null;
		collection = null;
	}

	private OfflineUserData(UUID uuid)
	{
		this.uuid = uuid;
		base64UUID = Utils.uuidToBase64(uuid);
		collection = DB.getCollection(base64UUID);
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

	public int getFuturyCoins()
	{
		UserData online = toOnline();
		if (online != null)
			return online.getFuturyCoins();

		return collection.find().first().getInteger("fc", 0);
	}

	public int getTurfuryCoins()
	{
		UserData online = toOnline();
		if (online != null)
			return online.getTurfuryCoins();

		return collection.find().first().getInteger("tc", 0);
	}

	public int getState()
	{
		UserData online = toOnline();
		if (online != null)
			return online.getState();

		return collection.find().first().getInteger("state", 0);
	}

	public int getGroup()
	{
		UserData online = toOnline();
		if (online != null)
			return online.getGroup();

		return collection.find().first().getInteger("group", 0);
	}

	public void setFuturyCoins(int futuryCoins)
	{
		UserData online = toOnline();
		if (online != null)
		{
			online.setFuturyCoins(futuryCoins);
			return;
		}

		collection.updateOne(EMPTY_DOCUMENT, new Document("fc", futuryCoins));
	}

	public void setTurfuryCoins(int turfuryCoins)
	{
		UserData online = toOnline();
		if (online != null)
		{
			online.setTurfuryCoins(turfuryCoins);
			return;
		}

		collection.updateOne(EMPTY_DOCUMENT, new Document("tc", turfuryCoins));
	}

	public void setState(int state)
	{
		UserData online = toOnline();
		if (online != null)
		{
			online.setState(state);
			return;
		}

		collection.updateOne(EMPTY_DOCUMENT, new Document("state", state));
	}

	public void setGroup(int group)
	{
		UserData online = toOnline();
		if (online != null)
		{
			online.setGroup(group);
			return;
		}

		collection.updateOne(EMPTY_DOCUMENT, new Document("group", group));
	}
}
