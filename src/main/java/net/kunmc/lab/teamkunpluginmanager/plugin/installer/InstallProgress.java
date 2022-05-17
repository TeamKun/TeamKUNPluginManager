package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.collector.DependsCollectCache;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Getter
public class InstallProgress<P extends Enum<P>>
{
    private static final HashMap<String, InstallProgress<?>> progressCaches;

    @Setter
    private P phase;

    private final List<String> upgraded;
    private final List<String> installed;
    private final List<String> removed;
    private final List<String> pending;

    static
    {
        progressCaches = new HashMap<>();
    }

    private final Path installTempDir;

    private final String installActionID;
    private final DependsCollectCache dependsCollectCache;

    private InstallProgress(@Nullable String id) throws IOException, SecurityException
    {
        this.upgraded = new ArrayList<>();
        this.installed = new ArrayList<>();
        this.removed = new ArrayList<>();
        this.pending = new ArrayList<>();

        this.phase = null;

        if (id == null)
            this.installActionID = UUID.randomUUID().toString().substring(0, 8);
        else
            this.installActionID = id;

        this.installTempDir = Files.createTempDirectory(
                TeamKunPluginManager.getPlugin().getDataFolder().toPath(),
                this.getInstallActionID()
        );

        this.dependsCollectCache = new DependsCollectCache(this.installActionID);

        progressCaches.put(this.getInstallActionID(), this);
    }

    public static <P extends Enum<P>> InstallProgress<P> of(@Nullable String id) throws IOException, SecurityException
    {
        if (id == null)
            return new InstallProgress<>(null);
        else
            return (InstallProgress<P>) progressCaches.get(id);
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

    public void finish()
    {
        try
        {
            FileUtils.forceDelete(this.installTempDir.toFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        progressCaches.remove(this.getInstallActionID());
    }
}
