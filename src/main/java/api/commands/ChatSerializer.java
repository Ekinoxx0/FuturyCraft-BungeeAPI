package api.commands;

import fr.skybeast.commandcreator.CommandSerializationException;
import fr.skybeast.commandcreator.CommandSerializer;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by SkyBeast on 18/02/17.
 */
public class ChatSerializer implements CommandSerializer<String>
{
	@Override
	public String serialize(String arg) throws CommandSerializationException
	{
		return ChatColor.translateAlternateColorCodes('&', arg);
	}

	@Override
	public String valueType()
	{
		return "Colorable String";
	}
}
