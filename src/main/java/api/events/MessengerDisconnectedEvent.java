package api.events;

import api.packets.MessengerClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 25/01/17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MessengerDisconnectedEvent extends Event
{
	private final MessengerClient client;
}
