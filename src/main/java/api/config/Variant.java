package api.config;

import java.util.List;

/**
 * Created by loucass003 on 21/12/16.
 */
public class Variant
{
	private int minServers;
	private int maxServers;
	private int slots;
	private int minRam;
	private int maxRam;

	private List<String> spigotArgs;
	private List<String> jvmArgs;
	private String spigotPath;
	private String propsPath;
	private String mapPath;

	public int getMinServers()
	{
		return minServers;
	}

	public int getMaxServers()
	{
		return maxServers;
	}

	public String getMapPath()
	{
		return mapPath;
	}

	public String getPropsPath()
	{
		return propsPath;
	}

	public String getSpigotPath()
	{
		return spigotPath;
	}

	public int getMaxRam()
	{
		return maxRam;
	}

	public int getMinRam()
	{
		return minRam;
	}

	public int getSlots()
	{
		return slots;
	}

	public List<String> getJvmArgs()
	{
		return jvmArgs;
	}

	public List<String> getSpigotArgs()
	{
		return spigotArgs;
	}

	@Override
	public String toString()
	{
		return "Variant{" +
				"minServers=" + minServers +
				", maxServers=" + maxServers +
				", slots=" + slots +
				", minRam=" + minRam +
				", maxRam=" + maxRam +
				", spigotArgs=" + spigotArgs +
				", jvmArgs=" + jvmArgs +
				", spigotPath='" + spigotPath + '\'' +
				", propsPath='" + propsPath + '\'' +
				", mapPath='" + mapPath + '\'' +
				'}';
	}
}
