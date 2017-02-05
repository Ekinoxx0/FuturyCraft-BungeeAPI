package api.perms;

import gnu.trove.list.TIntList;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loucass003 on 2/5/17.
 */
@Data
public class Group
{
	private final int id;
	private final String name;
	private final String prefix;
	private final String suffix;
	private final TIntList perms;
	private final ChatColor color;
	private final ChatColor chatColor;

	@SuppressWarnings("unchecked")
	public static Group fromDoc(Document doc)
	{
		return doc != null ?
				new Group(
					doc.getInteger("id"),
					doc.getString("name"),
					doc.getString("prefix"),
					doc.getString("suffix"),
					doc.get("perms", TIntList.class),
					ChatColor.getByChar((char) doc.get("color")),
					ChatColor.getByChar((char) doc.get("chatColor"))
				)
				: null;
	}

	public Document toDoc()
	{
		return new Document("id", id)
				.append("name", name)
				.append("prefix", prefix)
				.append("suffix", suffix)
				.append("perms", perms)
				.append("color", color.toString().replace("§", ""))
				.append("chatColor", chatColor.toString().replace("§", ""));
	}

	public boolean hasPerm(String perm)
	{
		for(int i : perms.toArray())
			if(perm.equals(PermissionsManager.perms.get(i)))
				return true;
		return false;
	}
}