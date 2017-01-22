package api.events;

import api.data.Server;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 07/01/2017.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NewConsoleLineEvent extends Event
{
	private final Server server;
	private final String line;
}
