package api.commands.permissons;

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
	private static final BaseComponent[] HELP = new ComponentBuilder("Usage: /group <add|set|rem|list>").color(ChatColor.RED).create();
	private static final BaseComponent[] HELP_ADD = new ComponentBuilder("Usage: /group add <name> <prefix> <suffix> <chatcolor> <color>").color(ChatColor.RED).create();
	private static final BaseComponent[] HELP_LIST = new ComponentBuilder("Usage: /group list").color(ChatColor.RED).create();
	private static final BaseComponent[] HELP_REM = new ComponentBuilder("Usage: /group rem <id>").color(ChatColor.RED).create();
	private static final BaseComponent[] HELP_SET = new ComponentBuilder("Usage: /group set <id> <prefix|suffix|chatcolor|color> <value>").color(ChatColor.RED).create();

	public GroupCommand()
	{
		super("group", "admin.group");
	}

	@Override
	public void execute(CommandSender sender, String[] args)
	{
		if(args.length < 1 || args.length == 1 && "help".equalsIgnoreCase(args[0]))
		{
			sender.sendMessage(HELP);
			return;
		}

		if("add".equalsIgnoreCase(args[0]))
		{

		}
		else if("list".equalsIgnoreCase(args[0]))
		{

		}
		else if("rem".equalsIgnoreCase(args[0]))
		{

		}
		else
			sender.sendMessage(HELP);
	}
}
