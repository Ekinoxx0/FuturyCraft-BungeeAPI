package api.commands.permissons;

import api.Main;
import api.perms.Group;
import gnu.trove.list.array.TIntArrayList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by loucass003 on 2/5/17.
 */
public class GroupCommand extends Command
{
	private static final BaseComponent[] HELP = new ComponentBuilder("Usage: /group <add|set|rem|list|perm|member>")
			.color(ChatColor.RED).create();
	private static final BaseComponent[] HELP_ADD = new ComponentBuilder("Usage: /group add {name} {prefix} {suffix} " +
			"{chatcolor} {color}").color(ChatColor.RED).create();
	private static final BaseComponent[] HELP_LIST = new ComponentBuilder("Usage: /group list").color(ChatColor.RED)
			.create();
	private static final BaseComponent[] HELP_REM = new ComponentBuilder("Usage: /group rem {name}").color(ChatColor
			.RED).create();
	private static final BaseComponent[] HELP_SET = new ComponentBuilder("Usage: /group set {name} " +
			"<prefix|suffix|chatcolor|color> {value}").color(ChatColor.RED).create();
	private static final BaseComponent[] HELP_PERM = new ComponentBuilder("Usage: /group perm {name} <add|rem|list> " +
			"{perm}").color(ChatColor.RED).create();
	private static final BaseComponent[] HELP_MEMBER = new ComponentBuilder("Usage: /group member {name} " +
			"<add|rem|list> {player}").color(ChatColor.RED).create();

	private static final BaseComponent[] GROUP_ALREADY_EXIST = new ComponentBuilder("This group already exist!").color
			(ChatColor.RED).create();
	private static final BaseComponent[] GROUP_NOT_EXIST = new ComponentBuilder("This group does not exist!").color
			(ChatColor.RED).create();

	private static final BaseComponent[] SETTER_NOT_EXIST = new ComponentBuilder("Setter does not exist!").color
			(ChatColor.RED).create();

	public GroupCommand()
	{
		super("group", "admin.group");
	}

	@Override
	public void execute(CommandSender sender, String[] args)
	{
		if (args.length < 1 || args.length == 1 && "help".equalsIgnoreCase(args[0]))
		{
			sender.sendMessage(HELP);
			return;
		}

		switch (args[0].toLowerCase())
		{
			case "add":
				addGroup(sender, args);
				break;
			case "set":
				setGroup(sender, args);
				break;
			case "rem":
				remGroup(sender, args);
				break;
			case "list":
				listGroups(sender);
				break;
			case "perm":
				permGroup(sender, args);
				break;
			case "members":
				membersGroup(sender, args);
				break;
			default:
				sender.sendMessage(HELP);
				break;
		}
	}

	private void addGroup(CommandSender sender, String[] args)
	{
		if (args.length != 6)
		{
			sender.sendMessage(HELP_ADD);
			return;
		}

		String name = args[1];

		if (Main.getInstance().getPermsManager().getGroupByName(name) != null)
		{
			sender.sendMessage(GROUP_ALREADY_EXIST);
			return;
		}

		String prefix = args[2].replace("&", "§");
		String suffix = args[3].replace("&", "§");
		String chatColor = args[4].replace("&", "§");
		String color = args[5].replace("&", "§");

		Main.getInstance().getPermsManager().addGroup(new Group(name, prefix, suffix, new TIntArrayList(), color,
				chatColor));
	}

	private void setGroup(CommandSender sender, String[] args)
	{
		if (args.length != 4)
		{
			sender.sendMessage(HELP_SET);
			return;
		}

		Group group = Main.getInstance().getPermsManager().getGroupByName(args[1]);

		if (group == null)
		{
			sender.sendMessage(GROUP_NOT_EXIST);
			return;
		}

		String var = args[2];
		String value = args[3];

		if ("prefix".equalsIgnoreCase(var))
			group.setPrefix(value);
		else if ("suffix".equalsIgnoreCase(var))
			group.setSuffix(value);
		else if ("color".equalsIgnoreCase(var))
			group.setColor(value);
		else if ("chatcolor".equalsIgnoreCase(var))
			group.setChatColor(value);
		else
		{
			sender.sendMessage(SETTER_NOT_EXIST);
			return;
		}

		Main.getInstance().getPermsManager().updateGroup(group);
	}

	private void remGroup(CommandSender sender, String[] args)
	{
		if (args.length != 2)
		{
			sender.sendMessage(HELP_REM);
			return;
		}

		Group group = Main.getInstance().getPermsManager().getGroupByName(args[1]);

		if (group == null)
		{
			sender.sendMessage(GROUP_NOT_EXIST);
			return;
		}

		Main.getInstance().getPermsManager().remGroup(group);
	}

	private void listGroups(CommandSender sender)
	{
		sender.sendMessage(new ComponentBuilder("Liste des groupes :").color(ChatColor.GREEN).create());
		Main.getInstance().getPermsManager().getGroups().forEach(g ->
				sender.sendMessage(new ComponentBuilder("name: ")
						.append(g.getName())
						.append(" displayName: ")
						.append(g.getColor())
						.append(g.getPrefix())
						.append(g.getName())
						.append(g.getSuffix())
						.create()
				)
		);
	}

	private void permGroup(CommandSender sender, String[] args)
	{
		if (args.length != 4)
		{
			sender.sendMessage(HELP_PERM);
			return;
		}


	}

	private void membersGroup(CommandSender sender, String[] args)
	{
		if (args.length != 4)
		{
			sender.sendMessage(HELP_MEMBER);
			return;
		}


	}
}
