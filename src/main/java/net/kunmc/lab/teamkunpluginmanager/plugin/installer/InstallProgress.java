package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class InstallProgress<P extends Enum<P>>
{
    @Setter
    private P phase;

    private final List<String> upgraded;
    private final List<String> installed;
    private final List<String> removed;
    private final List<String> pending;

    private final UUID installActionID;
    private final Path installTempDir;

    public InstallProgress(boolean createTempDir) throws IOException, SecurityException
    {
        this.upgraded = new ArrayList<>();
        this.installed = new ArrayList<>();
        this.removed = new ArrayList<>();
        this.pending = new ArrayList<>();

        this.phase = null;

        this.installActionID = UUID.randomUUID();

        if (!createTempDir)
        {
            this.installTempDir = null;
            return;
        }

        this.installTempDir = Files.createTempDirectory(
                TeamKunPluginManager.getPlugin().getDataFolder().toPath(),
                this.getInstallActionID().toString()
        );
    }

    public static InstallProgress<?> dummy()
    {
        try
        {
            return new InstallProgress<>(false);
        }
        catch (IOException e)
        { // should not happen
            throw new IllegalStateException(e);
        }
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
    }
}
