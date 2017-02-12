package api.config;

import lombok.Data;

/**
 * Created by loucass003 on 2/12/17.
 */
@Data
public class ConfigVolume
{
	private final String host;
	private final String container;
	private final boolean readOnly = false;
}
