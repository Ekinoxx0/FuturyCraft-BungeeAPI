package api.data;

import api.Main;
import api.perms.Group;
import api.utils.Utils;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

/**
 * Created by loucass003 on 2/7/17.
 */
public class RedisUser implements IUserData
{

	private final String redisPrefix;

	public RedisUser(String redisPrefix)
	{
		this.redisPrefix = redisPrefix;
	}

	@Override
	public int getFuturyCoins()
	{
		return Utils.stringToInt(Utils.returnRedis(jedis -> jedis.get(redisPrefix + ":fc")));
	}

	@Override
	public void setFuturyCoins(int fc)
	{
		Utils.doRedis(jedis -> jedis.set(redisPrefix + ":fc", Utils.intToString(fc)));
	}

	@Override
	public int getTurfuryCoins()
	{
		return Utils.stringToInt(Utils.returnRedis(jedis -> jedis.get(redisPrefix + ":tc")));
	}

	@Override
	public void setTurfuryCoins(int tc)
	{
		Utils.doRedis(jedis -> jedis.set(redisPrefix + ":tc", Utils.intToString(tc)));
	}

	@Override
	public Group getGroup()
	{
		return Main.getInstance().getPermsManager()
				.getGroupByName(Utils.returnRedis(jedis -> jedis.get(redisPrefix + ":group")));
	}

	@Override
	public void setGroup(Group g)
	{
		Utils.doRedis(jedis -> jedis.set(redisPrefix + ":group", g.getName()));
	}

	public Document toDocument(Transaction transaction)
	{
		Response<String> rFC = transaction.get(redisPrefix + ":fc");
		Response<String> rTC = transaction.get(redisPrefix + ":tc");
		Response<String> rGroup = transaction.get(redisPrefix + ":group");
		transaction.exec();

		Document doc = new Document();

		doc.put("fc", Utils.stringToInt(rFC.get()));
		doc.put("tc", Utils.stringToInt(rTC.get()));
		doc.put("group", rGroup.get());

		return doc;
	}

	public void remove(Jedis jedis)
	{
		jedis.del(
			redisPrefix + ":fc",
			redisPrefix + ":tc",
			redisPrefix + ":group"
		);
	}
}
