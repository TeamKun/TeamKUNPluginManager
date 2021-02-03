package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.utils.GitHubURLBuilder;

import java.util.ArrayList;
import java.util.Objects;

public class PluginCompacter
{
    private final ArrayList<PluginContainer> container;

    private final PluginContainer pre;

    public PluginCompacter()
    {
        this.container = new ArrayList<>();
        this.pre = new PluginContainer();
    }

    public void addPlugin(String name)
    {
        this.pre.pluginName = name;

        String orgName = TeamKunPluginManager.config.getString("gitHubName");
        String repoName = orgName + "/" + name;
        if (KnownPlugins.isKnown(name))
        { //TODO: URLがnullだった場合指定させる
            try
            {
                this.pre.downloadUrl = Objects.requireNonNull(KnownPlugins.getKnown(name)).url;
            }
            catch (Exception ignored) { }
        }
        else if (GitHubURLBuilder.isRepoExists(repoName))
            this.pre.downloadUrl = "https://github.com/" + repoName;

    }
}
