package api.perms;

import api.Main;
import api.perms.events.GroupRemovedEvent;
import api.utils.SimpleManager;
import api.utils.Utils;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by loucass003 on 2/5/17.
 */
public class PermissionsManager implements SimpleManager
{
	private final MongoDatabase permsDB = Main.getInstance().getMongoClient().getDatabase("perms");
	private final MongoCollection<Document> groupsCollection = permsDB.getCollection("groups");
	private final MongoCollection<Document> permsCollection = permsDB.getCollection("perms");

	private final Listen listener = new Listen();

	private final ReentrantLock groupsLock = new ReentrantLock();
	private static final List<Group> GROUPS = new ArrayList<>();
	public static TIntObjectMap<String> perms;
	private boolean init;

	@Override
	public void init()
	{
		if (init)
			throw new IllegalStateException("Already initialised!");
		ProxyServer.getInstance().getPluginManager().registerListener(Main.getInstance(), listener);
		perms = getPerms();
		init = true;
	}

	public void addGroup(Group g)
	{
		Utils.doLocked(() -> {
			GROUPS.add(g);
			addGroupToDB(g);
		}, groupsLock);
	}

	public void remGroup(Group g)
	{
		Utils.doLocked(() ->
		{
			GROUPS.remove(g);
			remGroupFromDB(g);
		}, groupsLock);
		ProxyServer.getInstance().getPluginManager().callEvent(new GroupRemovedEvent(g));
	}

	public Group getGroupByName(String name)
	{
		return Utils.returnLocked(() ->
					GROUPS.stream()
						.filter(g -> name.equals(g.getName()))
						.findFirst()
						.orElseGet(() -> {
							Document doc = groupsCollection.find(Filters.eq("name", name)).first();
							Group group = Group.fromDoc(doc);
							if(group != null) GROUPS.add(group);
							return group;
						}),
				groupsLock);
	}

	public Group getGroup(int id)
	{
		return Utils.returnLocked(() ->
						GROUPS.stream()
								.filter(g -> g.getId() == id)
								.findFirst()
								.orElseGet(() -> {
									Document doc = groupsCollection.find(Filters.eq("id", id)).first();
									Group group = Group.fromDoc(doc);
									if(group != null) GROUPS.add(group);
									return group;
								}),
				groupsLock);
	}

	private void addGroupToDB(Group g)
	{
		groupsCollection.insertOne(g.toDoc());
	}

	private void remGroupFromDB(Group g)
	{
		groupsCollection.deleteOne(Filters.eq("name", g.getId()));
	}

	private TIntObjectMap<String> getPerms()
	{
		TIntObjectMap<String> perms = new TIntObjectHashMap<>();
		FindIterable<Document> docs = permsCollection.find();
		if(docs == null)
			return perms;
		docs.forEach((Block<? super Document>) doc -> perms.put(doc.getInteger("id"), doc.getString("value")));
		return perms;
	}

	public class Listen implements Listener
	{
		private Listen() {}


	}
}
