package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.signals;

import lombok.Data;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Data
public class PluginLoadSignal implements InstallerSignal
{
    @NotNull
    private final Path pluginPath;
    @NotNull
    private final PluginDescriptionFile pluginDescription;

    public static class Pre extends PluginLoadSignal
    {
        public Pre(@NotNull Path pluginPath, @NotNull PluginDescriptionFile pluginDescription)
        {
            super(pluginPath, pluginDescription);
        }
    }

    public static class Post extends PluginLoadSignal
    {
        @NotNull
        @Getter
        private final Plugin plugin;

        public Post(@NotNull Path pluginPath, @NotNull PluginDescriptionFile pluginDescription, @NotNull Plugin plugin)
        {
            super(pluginPath, pluginDescription);
            this.plugin = plugin;
        }
    }
}
