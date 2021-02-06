package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import java.util.Map;

public class PluginContainer implements Cloneable
{
    public String pluginName;
    public String downloadUrl;
    public Map<String, Object> config;

    public void clean()
    {
        this.pluginName = null;
        this.downloadUrl = null;
        this.config = null;
    }

    @Override
    protected Object clone()
    {
        PluginContainer container = null;
        try
        {
            container = (PluginContainer) super.clone();
            return container;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return container;
    }
}
