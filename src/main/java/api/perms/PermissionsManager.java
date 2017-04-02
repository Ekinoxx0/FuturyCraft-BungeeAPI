package api.perms;

import api.Main;
import api.events.GroupRemovedEvent;
import api.events.GroupUpdatedEvent;
import api.utils.SimpleManager;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.ToString;
import lombok.extern.java.Log;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by loucass003 on 2/5/17.
 */
@ToString
@Log
public final class PermissionsManager implements SimpleManager
{
	private final MongoDatabase permsDB = Main.getInstance().getMongoClient().getDatabase("perms");
	private final MongoCollection<Document> groupsCollection = permsDB.getCollection("groups");
	private final MongoCollection<Document> permsCollection = permsDB.getCollection("perms");

	private static final List<Group> GROUPS = new CopyOnWriteArrayList<>();
	private TIntObjectMap<String> perms;

	private final Listen listener = new Listen();

	@Override
	public void init()
	{
		perms = getPerms();
	}

	public static PermissionsManager instance()
	{
		return Main.getInstance().getPermissionsManager();
	}

	public void addGroup(Group g)
	{
		GROUPS.add(g);
		addGroupToDB(g);
	}

	public void remGroup(Group g)
	{
		GROUPS.remove(g);
		remGroupFromDB(g);
		Main.callEvent(new GroupRemovedEvent(g));
	}

	public void updateGroup(Group g)
	{
		updateGroupToDB(g);
		Main.callEvent(new GroupUpdatedEvent(g));
	}

	public Group getGroupByName(String name)
	{
		if (name == null)
			return null;
		return GROUPS.stream()
				.filter(g -> g.getName().equals(name))
				.findFirst()
				.orElseGet(() ->
				{
					Document doc = groupsCollection.find(Filters.eq("name", name)).first();
					Group group = Group.fromDoc(doc);
					if (group != null) GROUPS.add(group);
					return group;
				});
	}

	public List<Group> getGroups()
	{
		int count = (int) groupsCollection.count();
		if (GROUPS.size() == count)
			return GROUPS;
		FindIterable<Document> docs = groupsCollection.find();
		docs.forEach((Block<? super Document>) doc -> getGroupByName(Group.fromDoc(doc).getName()));
		return GROUPS;
	}

	private void addGroupToDB(Group g)
	{
		groupsCollection.insertOne(g.toDoc());
	}

	private void remGroupFromDB(Group g)
	{
		groupsCollection.deleteOne(Filters.eq("name", g.getName()));
	}

	private void updateGroupToDB(Group g)
	{
		groupsCollection.updateOne(Filters.eq("name", g.getName()), g.toDoc());
	}

	private TIntObjectMap<String> setupPerms()
	{
		TIntObjectMap<String> perms = new TIntObjectHashMap<>();
		FindIterable<Document> docs = permsCollection.find();
		if (docs == null)
			return perms;
		docs.forEach((Block<? super Document>) doc -> perms.put(doc.getInteger("id"), doc.getString("value")));
		return perms;
	}

	public TIntObjectMap<String> getPerms()
	{
		return perms;
	}

	private class Listen implements Listener
	{
		private Listen() {}

		@EventHandler
		public void onGroupUpdated(GroupUpdatedEvent e)
		{
			//Main.getInstance().getServerMessenger();
		}
	}
}
