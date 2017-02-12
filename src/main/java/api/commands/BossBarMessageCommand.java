package api.commands;

import api.Main;
import api.deployer.DeployerServer;
import api.packets.server.BossBarMessagesPacket;
import api.utils.Utils;
import api.utils.UtilsListener;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import org.bson.Document;

/**
 * Created by loucass003 on 2/3/17.
 */
public class BossBarMessageCommand extends Command
{
	private static final MongoCollection<Document> COLLECTION = Main.getInstance().getMainDatabase()
			.getCollection("bossBarMessages");

	private static final BaseComponent[] HELP = new ComponentBuilder("Usage: /bossbar <add|rem|list>")
			.color(ChatColor.RED).create();
	private static final BaseComponent[] ADD_HELP = new ComponentBuilder("Usage: /bossbar add {time (s)} {message}")
			.color(ChatColor.RED).create();
	private static final BaseComponent[] LIST_HELP = new ComponentBuilder("Usage: /bossbar list")
			.color(ChatColor.RED).create();
	private static final BaseComponent[] REM_HELP = new ComponentBuilder("Usage: /bossbar rem {id}").color(ChatColor
			.RED).create();

	private static final BaseComponent[] NOT_A_NUMBER = new ComponentBuilder("Param used is not a number")
			.color(ChatColor.RED).create();
	private static final BaseComponent[] NOT_EXISTING = new ComponentBuilder("Message id does not exist !")
			.color(ChatColor.RED).create();

	private static final BaseComponent[] ADD_SUCCESS = new ComponentBuilder("Le message a bien été ajouté")
			.color(ChatColor.GREEN).create();
	private static final BaseComponent[] REM_SUCCESS = new ComponentBuilder("Le message a bien été supprimé")
			.color(ChatColor.GREEN).create();

	public BossBarMessageCommand()
	{
		super("bossbar", "admin.bossbar", "bb");
	}

	@Override
	public void execute(CommandSender sender, String[] args)
	{
		if (args.length < 1)
		{
			sender.sendMessage(HELP);
			return;
		}

		if ("add".equalsIgnoreCase(args[0]))
		{
			if (args.length < 3)
			{
				sender.sendMessage(ADD_HELP);
				return;
			}

			Integer time = Utils.isNumeric(args[1]);
			if (time == null)
			{
				sender.sendMessage(NOT_A_NUMBER);
				return;
			}

			StringBuilder sb = new StringBuilder();
			for (int i = 2; i < args.length; i++)
				sb.append(args[i]).append(i == args.length - 1 ? "" : " ");
			addMessage(time, sb.toString().trim());

			Main.getInstance().getDataManager().forEachServersByType(server ->
							Main.getInstance().getUtilsListener().sendBossBarMessagesPacket(server),
					DeployerServer.ServerType.LOBBY
			);
			sender.sendMessage(ADD_SUCCESS);
		}
		else if ("list".equalsIgnoreCase(args[0]))
		{
			sender.sendMessage(new ComponentBuilder("Liste des messages :").color(ChatColor.GREEN).create());

			int count = 0;
			FindIterable<Document> fi = COLLECTION.find();
			for (Document doc : fi)
			{
				count++;
				ComponentBuilder builder = new ComponentBuilder(String.valueOf(count));
				builder.append(" " + doc.get("time"));
				builder.append(" " + doc.get("message"));
				sender.sendMessage(builder.create());
			}
		}
		else if ("rem".equalsIgnoreCase(args[0]))
		{
			if (args.length != 2)
			{
				sender.sendMessage(REM_HELP);
				return;
			}

			Integer offset = Utils.isNumeric(args[1]);
			if (offset == null)
			{
				sender.sendMessage(NOT_A_NUMBER);
				return;
			}

			Document fi = COLLECTION.find().limit(offset).skip(offset - 1).first();

			if (fi == null)
			{
				sender.sendMessage(NOT_EXISTING);
				return;
			}

			UtilsListener.BOSS_BAR_MESSAGES.remove(offset - 1);
			COLLECTION.deleteOne(fi);
			sender.sendMessage(REM_SUCCESS);
			Main.getInstance().getDataManager().forEachServersByType(server ->
							Main.getInstance().getUtilsListener().sendBossBarMessagesPacket(server),
					DeployerServer.ServerType.LOBBY
			);
		}
		else
			sender.sendMessage(HELP);
	}

	public void addMessage(int time, String message)
	{
		Document doc = new Document();
		doc.put("time", time);
		doc.put("message", message);
		COLLECTION.insertOne(doc);
		UtilsListener.BOSS_BAR_MESSAGES.add(new BossBarMessagesPacket.MessageData(message, time));
	}
}
