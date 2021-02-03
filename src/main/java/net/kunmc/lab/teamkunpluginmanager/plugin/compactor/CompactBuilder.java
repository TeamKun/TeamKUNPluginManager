package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.utils.GitHubURLBuilder;
import org.apache.commons.lang.ArrayUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

public class CompactBuilder
{

    private final PluginContainer pre;

    private final PluginCompacter pc;

    private BuildResult[] rs;

    public CompactBuilder(PluginCompacter comp)
    {
        this.pre = new PluginContainer();
        this.pc = comp;
        this.rs = new BuildResult[]{};
    }

    public PluginCompacter build()
    {
        pc.apply(pre);
        pre.clean();

        return pc;
    }

    public CompactBuilder addPlugin(String name)
    {
        rs = new BuildResult[]{};

        this.pre.pluginName = name;

        String orgName = TeamKunPluginManager.config.getString("gitHubName");
        String repoName = orgName + "/" + name;
        if (KnownPlugins.isKnown(name))
        {
            try
            {
                this.pre.downloadUrl = Objects.requireNonNull(KnownPlugins.getKnown(name)).url;
            }
            catch (Exception ignored)
            {
                rs = (BuildResult[]) ArrayUtils.add(rs, BuildResult.DOWNLOAD_LINK_RESOLVE_FAILED);
            }
        }
        else if (GitHubURLBuilder.isRepoExists(repoName))
        {
            String preUrl = "https://github.com/" + repoName;
            String url = GitHubURLBuilder.urlValidate(preUrl);
            if (!url.startsWith("ERROR "))
                this.pre.downloadUrl = url;
            else
                rs = (BuildResult[]) ArrayUtils.add(rs, BuildResult.DOWNLOAD_LINK_RESOLVE_FAILED);
        }
        else
            rs = (BuildResult[]) ArrayUtils.add(rs, BuildResult.DOWNLOAD_LINK_RESOLVE_FAILED);

        return this;
    }

    public CompactBuilder applyUrl(String url)
    {
        this.pre.downloadUrl = url;
        return this;
    }

    public CompactBuilder applyConfig(LinkedHashMap<String, Object> map)
    {
        this.pre.config = map;
        return this;
    }
}
