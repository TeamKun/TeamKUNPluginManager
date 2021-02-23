package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import net.kunmc.lab.teamkunpluginmanager.utils.PluginResolver;
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

        String url = PluginResolver.asUrl(name);

        if (url.startsWith("ERROR"))
            this.pre.downloadUrl = url;
        else
            this.rs = (BuildResult[]) ArrayUtils.add(this.rs, BuildResult.DOWNLOAD_LINK_RESOLVE_FAILED);

        this.pre.downloadUrl = url;

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
