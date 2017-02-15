package api.config;

import lombok.Data;

import java.io.File;

/**
 * Created by loucass003 on 2/12/17.
 */
@Data
public class ConfigVolume
{
	private final File host;
	private final String container;
	private boolean readOnly;
}
