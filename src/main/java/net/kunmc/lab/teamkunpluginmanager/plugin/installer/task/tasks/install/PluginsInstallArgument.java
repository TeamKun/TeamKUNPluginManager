package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.DependencyElement;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * プラグインのインストール時に渡される引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class PluginsInstallArgument extends TaskArgument
{
    /**
     * プラグインのファイルのパスです。
     */
    @NotNull
    Path pluginPath;
    /**
     * プラグインのプラグイン情報ファイルです。
     */
    @NotNull
    PluginDescriptionFile pluginDescription;

    /**
     * 依存関係の要素です。
     */
    @NotNull
    List<DependencyElement> dependencies;
}
