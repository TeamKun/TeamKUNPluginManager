package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.utils.GitHubURLBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.ArrayUtils;

import java.util.Map;
import java.util.Objects;

public class CompactBuilder implements Cloneable
{

    private PluginContainer pre;

    private final PluginCompacter pc;

    private BuildResult[] rs;

    public CompactBuilder(PluginCompacter comp)
    {
        this.pre = new PluginContainer();
        this.pc = comp;
        this.rs = new BuildResult[]{};
    }

    @Override
    protected Object clone()
    {
        CompactBuilder builder = null;
        try
        {
            builder = (CompactBuilder) super.clone();
            builder.pre = (PluginContainer) this.pre.clone();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return builder;
    }

    public PluginContainer build()
    {
        return pre;
    }

    public PluginContainer getPre()
    {
        return pre;
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
        this.rs = (BuildResult[]) ArrayUtils.removeElement(this.rs, BuildResult.DOWNLOAD_LINK_RESOLVE_FAILED);
        return this;
    }

    public CompactBuilder applyConfig(Map<String, Object> map)
    {
        this.pre.config = map;
        return this;
    }

    public boolean isResolveFailed()
    {
        return ArrayUtils.contains(this.rs, BuildResult.DOWNLOAD_LINK_RESOLVE_FAILED);
    }
}
