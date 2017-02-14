package api.config;

import lombok.Data;

import java.util.Map;

/**
 * Created by loucass003 on 2/14/17.
 */
@Data
public class ServerPattern
{
	private final String name;
	private final Map<String, String> labels;
	private final Variant variant;

	public boolean has(String key, String value)
	{
		return value.equalsIgnoreCase(labels.get(key));
	}

	public static ServerPattern of(Map<String, String> labels, int variantOffset)
	{
		ServerConfig config = ServerConfig.getByLabels(labels).get(0);
		if (config == null)
			return null;
		Variant v = config.getVariants().get(0);
		return v != null ? new ServerPattern(config.getName(), config.getLabels(), v) : null;
	}

	public static ServerPattern of(Map<String, String> labels, Variant v)
	{
		return of(ServerConfig.getByLabels(labels).get(0), v);
	}

	public static ServerPattern of(ServerConfig config, Variant v)
	{
		return config != null && v != null ? new ServerPattern(config.getName(), config.getLabels(), v) : null;
	}
}
