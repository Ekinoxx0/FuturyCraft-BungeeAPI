package api.data;

import api.Main;
import api.perms.Group;
import api.utils.Utils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Created by loucass003 on 2/7/17.
 */
public class MongoUser implements IUserData
{
	private static final Document EMPTY_DOCUMENT = new Document();
	private static final MongoDatabase DB = Main.getInstance().getMongoClient().getDatabase("users");
	private final MongoCollection<Document> collection;
	private final String redisPrefix;

	public MongoUser(String base64Uuid, String redisPrefix)
	{
		collection = DB.getCollection(base64Uuid);
		this.redisPrefix = redisPrefix;
	}

	@Override
	public int getFuturyCoins()
	{
		return collection.find().first().getInteger("fc", 0);
	}

	@Override
	public void setFuturyCoins(int fc)
	{
		collection.updateOne(EMPTY_DOCUMENT, new Document("fc", fc));
	}

	@Override
	public int getTurfuryCoins()
	{
		return collection.find().first().getInteger("tc", 0);
	}

	@Override
	public void setTurfuryCoins(int tc)
	{
		collection.updateOne(EMPTY_DOCUMENT, new Document("tc", tc));
	}

	@Override
	public Group getGroup()
	{
		return Main.getInstance().getPermsManager().getGroupByName(collection.find().first().getString("group"));
	}

	@Override
	public void setGroup(Group g)
	{
		collection.updateOne(EMPTY_DOCUMENT, new Document("group", g.getName()));
	}

	public Transaction toTransaction(Document doc, Jedis jedis)
	{
		Transaction transaction = jedis.multi();
		transaction.set(redisPrefix + ":fc", Utils.intToString(doc.getInteger("fc", 0)));
		transaction.set(redisPrefix + ":tc", Utils.intToString(doc.getInteger("tc", 0)));
		transaction.set(redisPrefix + ":group", Utils.getString(doc, "group", "default"));
		return transaction;
	}
}
