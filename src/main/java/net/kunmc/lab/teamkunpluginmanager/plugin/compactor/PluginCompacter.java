package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import com.google.gson.Gson;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.utils.GitHubURLBuilder;

import java.util.ArrayList;
import java.util.Objects;

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
