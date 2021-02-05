package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import com.google.gson.Gson;

import java.util.ArrayList;

public class PluginCompacter
{
    private final ArrayList<PluginContainer> container;

    public PluginCompacter()
    {
        this.container = new ArrayList<>();
    }

    public void apply(PluginContainer pc)
    {
        this.container.add(pc);
    }

    public CompactBuilder builder()
    {
        return new CompactBuilder(this);
    }

    public String build()
    {
        return new Gson().toJson(this.container);
    }

}
