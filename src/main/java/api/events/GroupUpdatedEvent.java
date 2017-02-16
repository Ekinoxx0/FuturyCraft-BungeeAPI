package api.events;

import api.perms.Group;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by loucass003 on 2/6/17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GroupUpdatedEvent extends Event
{
	private final Group group;
}
