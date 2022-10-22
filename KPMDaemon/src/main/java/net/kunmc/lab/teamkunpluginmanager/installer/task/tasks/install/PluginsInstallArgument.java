package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.install;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.installer.task.TaskArgument;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.teamkunpluginmanager.kpminfo.KPMInformationFile;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * プラグインのKPM情報ファイルです。
     */
    @Nullable
    KPMInformationFile kpmInformation;

    /**
     * 依存関係の要素です。
     */
    @NotNull
    List<DependencyElement> dependencies;
}
