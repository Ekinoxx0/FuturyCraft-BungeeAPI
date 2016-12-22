package api.config;

import api.deployer.Lobby;
import api.utils.FileAdaptater;
import api.utils.Utils;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by loucass003 on 21/12/16.
 */
public class DeployerConfig
{
    private File baseDir;
    private File deployerDir;
    private List<Template.LobbyTemplate> lobbies;
    private List<Template> games;

    public static DeployerConfig load(File f)
    {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(File.class, new FileAdaptater());
        gson.excludeFieldsWithModifiers(Modifier.PROTECTED);
        return gson.create().fromJson(Utils.readFile(f), DeployerConfig.class);
    }

    public File getDeployerDir()
    {
        return new File(getBaseDir(), deployerDir.getAbsolutePath());
    }

    public File getBaseDir()
    {
        return baseDir;
    }

    public List<Template.LobbyTemplate> getLobbies()
    {
        return lobbies;
    }

    public List<Template> getGames()
    {
        return games;
    }

    public List<Template.LobbyTemplate> getLobbiesByType(Lobby.LobbyType type)
    {
        return lobbies.stream()
                .filter(lobbyTemplate -> lobbyTemplate.getType().equals(type))
                .collect(Collectors.toList());
    }
}