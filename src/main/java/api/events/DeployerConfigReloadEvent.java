package api.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.plugin.Event;

/**
 * Created by SkyBeast on 12/02/17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeployerConfigReloadEvent extends Event
{
}
