package api.config;

import api.utils.FileAdapter;
import api.utils.Utils;
import com.google.gson.GsonBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Created by loucass003 on 21/12/16.
 */
@ToString
@Getter
@EqualsAndHashCode
public class DeployerConfig
{
	private File baseDir;
	private File deployerDir;
	private int maxSlots;
	private List<ServerConfig> servers;
	private int sendBufferSize;

	public static DeployerConfig load(File f)
	{
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(File.class, FileAdapter.INSTANCE);
		gson.excludeFieldsWithModifiers(Modifier.TRANSIENT);
		return gson.create().fromJson(Utils.readFile(f), DeployerConfig.class);
	}
}