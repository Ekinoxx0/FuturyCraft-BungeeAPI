package api.commands;

import api.Main;
import api.data.ServerDataManager;
import api.packets.server.BossBarMessagesPacket;
import api.utils.UtilsListener;
import com.mongodb.client.MongoCollection;
import fr.skybeast.commandcreator.Arg;
import fr.skybeast.commandcreator.Command;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bson.Document;

/**
 * Created by loucass003 on 2/3/17.
 */
@Command(value = "bossbar", permissions = "admin.bossbar", aliases = "bb")
public final class BossBarMessageCommand
{
	@Getter
	private static final MongoCollection<Document> COLLECTION = Main.getInstance().getMainDatabase()
			.getCollection("bossBarMessages");

	private static final BaseComponent[] NOT_EXISTING = new ComponentBuilder("Aucun message avec cet ID trouvé.")
			.color(ChatColor.RED).create();

	private static final BaseComponent[] MESSAGE_LIST_HEADER = new ComponentBuilder("Liste des messages:")
			.color(ChatColor.GREEN).create();
	private static final BaseComponent[] ADD_SUCCESS = new ComponentBuilder("Le message a bien été ajouté")
			.color(ChatColor.GREEN).create();
	private static final BaseComponent[] REM_SUCCESS = new ComponentBuilder("Le message a bien été supprimé")
			.color(ChatColor.GREEN).create();

	@Command
	public static void add(CommandSender sender,
	                       @Arg(value = "temps", desc = "en secondes") int time,
	                       @Arg("message") String... message)
	{
		String fullMessage = String.join(" ", message);

		addMessage(time, fullMessage);

		ServerDataManager.instance().forEachServersByType(server ->
				UtilsListener.instance().sendBossBarMessagesPacket(server), "lobby");

		sender.sendMessage(ADD_SUCCESS);
	}

	@Command
	public static void list(CommandSender sender)
	{
		sender.sendMessage(MESSAGE_LIST_HEADER);

		int count = 0;
		for (Document doc : COLLECTION.find())
			sender.sendMessage(new ComponentBuilder(count++ + " " + doc.get("time") +
					" " + doc.get("message")).create());
	}

	@Command
	public static void remove(CommandSender sender,
	                          @Arg("ID") int offset)
	{
		Document fi = COLLECTION.find().limit(offset).skip(offset - 1).first();

		if (fi == null)
		{
			sender.sendMessage(NOT_EXISTING);
			return;
		}

		UtilsListener.instance().removeBossBarMessage(offset - 1);
		COLLECTION.deleteOne(fi);
		ServerDataManager.instance().forEachServersByType(server ->
				UtilsListener.instance().sendBossBarMessagesPacket(server), "lobby");

		sender.sendMessage(REM_SUCCESS);
	}

	private static void addMessage(int time, String message)
	{
		Document doc = new Document();
		doc.put("time", time);
		doc.put("message", message);
		COLLECTION.insertOne(doc);
		UtilsListener.instance().addBossBarMessage(new BossBarMessagesPacket.MessageData(message, time));
	}

	private BossBarMessageCommand() {}
}
