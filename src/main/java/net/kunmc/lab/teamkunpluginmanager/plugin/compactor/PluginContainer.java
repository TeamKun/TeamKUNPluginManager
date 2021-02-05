package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import java.util.Map;

public class PluginContainer
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
}
