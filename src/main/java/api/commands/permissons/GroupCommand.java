package api.commands.permissons;

import api.commands.ChatSerializer;
import api.perms.Group;
import api.perms.PermissionsManager;
import fr.skybeast.commandcreator.*;
import gnu.trove.list.array.TIntArrayList;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Created by loucass003 on 2/5/17.
 */
@Command(value = "group", permissions = "admin.group")
public final class GroupCommand
{
	private static final BaseComponent[] GROUP_ALREADY_EXIST = new ComponentBuilder("Ce group éxiste déjà !")
			.color(ChatColor.RED).create();
	private static final BaseComponent[] GROUP_LIST = new ComponentBuilder("Liste des groupes:")
			.color(ChatColor.GREEN).create();

	@Command
	public static void add(CommandSender sender,
	                       @Arg("nom") String name,
	                       @Serial(ChatSerializer.class) @Arg("prefix") String prefix,
	                       @Serial(ChatSerializer.class) @Arg("suffix") String suffix,
	                       @Serial(ChatSerializer.class) @Arg("couleur de chat") String chatcolor,
	                       @Serial(ChatSerializer.class) @Arg("couleur") String color)
	{

		if (PermissionsManager.instance().getGroupByName(name) != null)
		{
			sender.sendMessage(GROUP_ALREADY_EXIST);
			return;
		}

		PermissionsManager.instance().addGroup(new Group(name, prefix, suffix, new TIntArrayList(), color,
				chatcolor));
	}

	@Command
	public static void set(CommandSender sender,
	                       @Serial(GroupSerializer.class) @Arg("groupe") Group group,
	                       @Arg("champ") GroupField field,
	                       @Serial(ChatSerializer.class) @Arg("valeur") String value)
	{
		switch (field)
		{
			case PREFIX:
				group.setPrefix(value);
				break;
			case SUFFIX:
				group.setSuffix(value);
				break;
			case COLOR:
				group.setColor(value);
				break;
			case CHATCOLOR:
				group.setChatColor(value);
				break;
		}

		PermissionsManager.instance().updateGroup(group);
	}

	@Command
	public static void remove(CommandSender sender,
	                          @Serial(GroupSerializer.class) @Arg("groupe") Group group)
	{
		PermissionsManager.instance().remGroup(group);
	}

	@Command
	public static void list(CommandSender sender)
	{
		sender.sendMessage(GROUP_LIST);

		PermissionsManager.instance().getGroups().forEach(g ->
				sender.sendMessage(new TextComponent("nom: " + g.getName() + " affichage: " +
						g.getColor() + g.getPrefix() + g.getName() + g.getSuffix()))
		);
	}

	public static class GroupSerializer implements CommandSerializer<Group>
	{
		@Override
		public Group serialize(String arg) throws CommandSerializationException
		{
			Group group = PermissionsManager.instance().getGroupByName(arg);

			if (group == null)
				throw new CommandSerializationException("Impossible de trouver le groupe " + arg);
			return group;
		}

		@Override
		public String valueType()
		{
			return "Group";
		}
	}

	@AllArgsConstructor
	public enum GroupField
	{
		PREFIX("prefix"),
		SUFFIX("suffix"),
		COLOR("color"),
		CHATCOLOR("chatcolor");

		private final String name;

		@Override
		public String toString()
		{
			return name;
		}
	}

	private GroupCommand() {}
}
