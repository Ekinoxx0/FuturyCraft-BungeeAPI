package api.commands;

import api.data.Server;
import api.packets.server.DispatchCommandPacket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created by SkyBeast on 05/01/2017.
 */
public class DispatchCommand extends Command
{
	private final static BaseComponent[] HELP = new ComponentBuilder("Usage: /dispatch {id} {command...}").color
			(ChatColor.RED).create();
	private final static BaseComponent[] NOT_FOUND = new ComponentBuilder("Server ID not found").color(ChatColor.RED)
			.create();

	public DispatchCommand()
	{
		super("dispatch", "admin.dispatch", "cmd", "dp");
	}

	@Override
	public void execute(CommandSender sender, String[] args)
	{
		if (args.length < 2)
		{
			sender.sendMessage(HELP);
			return;
		}

		Server server = Server.get(args[0]);
		if (server == null)
		{
			sender.sendMessage(NOT_FOUND);
			return;
		}

		server.getMessenger().sendPacket(new DispatchCommandPacket(backToString(1, args)));
	}

	private String backToString(int start, String[] args)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < args.length; i++)
			builder.append(args[i]);

		return builder.toString();
	}
}
