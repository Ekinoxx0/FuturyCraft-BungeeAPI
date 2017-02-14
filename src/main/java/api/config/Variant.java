package api.config;

import lombok.Data;

import java.util.List;

/**
 * Created by loucass003 on 2/14/17.
 */
@Data
public class Variant
{
	private final String name;
	private final int slots;
	private final int maxRam;
	private final String img;
	private final List<ConfigVolume> volumes;
}
