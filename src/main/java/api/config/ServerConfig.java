package api.config;

import api.deployer.Deployer;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by loucass003 on 2/14/17.
 */
@Data
public class ServerConfig
{
	private final String name;
	private final Map<String, String> labels;
	private final List<Variant> variants;

	public boolean has(String key, String value)
	{
		return value.equalsIgnoreCase(labels.get(key));
	}

	public static List<ServerConfig> getByLabels(Map<String, String> labels)
	{
		List<ServerConfig> configs = Deployer.instance().getConfig().getServers();
		return configs.stream()
				.filter(config -> search(labels, config.labels))
				.collect(Collectors.toList());
	}

	private static boolean search(Map<String, String> search, Map<String, String> data)
	{
		return data.entrySet().containsAll(search.entrySet());
	}
}
