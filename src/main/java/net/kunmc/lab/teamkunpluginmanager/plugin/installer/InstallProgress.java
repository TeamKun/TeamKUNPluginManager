package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class InstallProgress
{
    private final List<String> upgraded;
    private final List<String> installed;
    private final List<String> removed;
    private final List<String> pending;

    public InstallProgress()
    {
        this.upgraded = new ArrayList<>();
        this.installed = new ArrayList<>();
        this.removed = new ArrayList<>();
        this.pending = new ArrayList<>();
    }

    private void removeFromAll(@NotNull String name)
    {
        this.upgraded.remove(name);
        this.installed.remove(name);
        this.removed.remove(name);
        this.pending.remove(name);
    }

    public void addUpgraded(@NotNull String pluginName)
    {
        this.removeFromAll(pluginName);

        this.upgraded.add(pluginName);
    }

    public void addInstalled(@NotNull String pluginName)
    {
        this.removeFromAll(pluginName);

        this.installed.add(pluginName);
    }

    public void addRemoved(@NotNull String pluginName)
    {
        this.removeFromAll(pluginName);

        this.removed.add(pluginName);
    }

    public void addPending(@NotNull String pluginName)
    {
        this.removeFromAll(pluginName);

        this.pending.add(pluginName);
    }
}
