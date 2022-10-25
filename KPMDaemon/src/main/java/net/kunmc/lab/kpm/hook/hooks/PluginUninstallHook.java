package net.kunmc.lab.kpm.hook.hooks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.kpm.hook.KPMHook;
import net.kunmc.lab.kpm.kpminfo.KPMInformationFile;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * プラグインがアンインストールされるときに呼び出されるフックです。
 */
@Getter
@AllArgsConstructor
public class PluginUninstallHook implements KPMHook
{
    /**
     * プラグイン情報ファイルです。
     */
    @NotNull
    private final PluginDescriptionFile pluginDescription;

    /**
     * KPM情報ファイルです。
     */
    @Nullable
    private final KPMInformationFile kpmInfo;

    public PluginUninstallHook(@NotNull PluginDescriptionFile pluginDescription)
    {
        this(pluginDescription, null);
    }

    @Getter
    public static class Pre extends PluginUninstallHook
    {
        @NotNull
        private final Plugin plugin;

        public Pre(@NotNull PluginDescriptionFile pluginDescription, @Nullable KPMInformationFile kpmInfo, @NotNull Plugin plugin)
        {
            super(pluginDescription, kpmInfo);
            this.plugin = plugin;
        }

        public Pre(@NotNull PluginDescriptionFile pluginDescription, @NotNull Plugin plugin)
        {
            super(pluginDescription);
            this.plugin = plugin;
        }
    }

    public static class Post extends PluginUninstallHook
    {
        public Post(@NotNull PluginDescriptionFile pluginDescription, @Nullable KPMInformationFile kpmInfo)
        {
            super(pluginDescription, kpmInfo);
        }

        public Post(@NotNull PluginDescriptionFile pluginDescription)
        {
            super(pluginDescription);
        }
    }
}
