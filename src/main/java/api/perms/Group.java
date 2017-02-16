package api.perms;

import gnu.trove.list.TIntList;
import lombok.Data;
import org.bson.Document;

/**
 * Created by loucass003 on 2/5/17.
 */
@Data
public class Group
{
	private String name;
	private String prefix;
	private String suffix;
	private TIntList perms;
	private String color;
	private String chatColor;

	public Group(String name, String prefix, String suffix, TIntList perms, String color, String chatColor)
	{
		this.name = name;
		this.prefix = prefix;
		this.suffix = suffix;
		this.perms = perms;
		this.color = color;
		this.chatColor = chatColor;
	}

	@SuppressWarnings("unchecked")
	public static Group fromDoc(Document doc)
	{
		return doc != null ?
				new Group(
						doc.getString("name"),
						doc.getString("prefix"),
						doc.getString("suffix"),
						doc.get("perms", TIntList.class),
						doc.getString("color"),
						doc.getString("chatColor")
				)
				: null;
	}

	public Document toDoc()
	{
		return new Document("name", name)
				.append("prefix", prefix)
				.append("suffix", suffix)
				.append("perms", perms)
				.append("color", color)
				.append("chatColor", chatColor);
	}

	public boolean hasPerm(String perm)
	{
		return hasPerm(this, perm);
	}

	public static boolean hasPerm(Group group, String perm)
	{
		if (group == null)
			return false;

		for (int i : group.getPerms().toArray())
			if (perm.equals(PermissionsManager.instance().getPerms().get(i)))
				return true;
		return false;
	}
}