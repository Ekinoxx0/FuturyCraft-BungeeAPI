package api.config;

import lombok.Data;

import java.util.List;

/**
 * Created by loucass003 on 21/12/16.
 */
@Data
public class Variant
{
	private final int minServers;
	private final int maxServers;
	private final int slots;
	private final int minRam;
	private final int maxRam;

	private final String img;
	private List<ConfigVolume> volumes;
}
