package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import org.apache.commons.lang.ArrayUtils;

import java.util.Map;

public class CompactBuilder implements Cloneable
{

    private final PluginCompacter pc;
    private PluginContainer pre;
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

        ResolveResult result = TeamKunPluginManager.getPlugin().getResolver().resolve(name);

        if (result instanceof ErrorResult)
            this.rs = (BuildResult[]) ArrayUtils.add(this.rs, BuildResult.DOWNLOAD_LINK_RESOLVE_FAILED);
        else if (result instanceof SuccessResult)
            this.pre.downloadUrl = ((SuccessResult) result).getDownloadUrl();

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
