package api.config;

import api.lobby.LobbyType;
import api.utils.FileAdapter;
import api.utils.Utils;
import com.google.gson.GsonBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by loucass003 on 21/12/16.
 */
@ToString
@EqualsAndHashCode
public class DeployerConfig
{
	@Getter
	private File baseDir;
	private String deployerDir;
	@Getter
	private List<Template.LobbyTemplate> lobbies;
	@Getter
	private List<Template> games;
	private transient File deployerDirCache;

	public static DeployerConfig load(File f)
	{
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(File.class, new FileAdapter());
		gson.excludeFieldsWithModifiers(Modifier.TRANSIENT);
		return gson.create().fromJson(Utils.readFile(f), DeployerConfig.class);
	}

	public File getDeployerDir()
	{
		if (deployerDirCache == null)
			deployerDirCache = new File(getBaseDir(), deployerDir);
		return deployerDirCache;
	}

	public List<Template.LobbyTemplate> getLobbiesByType(LobbyType type)
	{
		return lobbies.stream()
				.filter(lobbyTemplate -> lobbyTemplate.getType() == type)
				.collect(Collectors.toList());
	}
}