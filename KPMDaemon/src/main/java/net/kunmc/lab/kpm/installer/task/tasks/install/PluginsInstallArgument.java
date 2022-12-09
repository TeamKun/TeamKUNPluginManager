package net.kunmc.lab.kpm.installer.task.tasks.install;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.task.TaskArgument;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.kpm.kpminfo.KPMInformationFile;
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
     * これを {@code null} にした場合, 依存関係のみがインストールされます。
     * また, {@code null} 以外の場合, {@link #pluginDescription} は {@code null} にできません。
     */
    @Nullable
    Path pluginPath;
    /**
     * プラグインのプラグイン情報ファイルです。
     * これを {@code null} にした場合, 依存関係のみがインストールされます。
     * また, {@code null} 以外の場合, {@link #pluginPath} は {@code null} にできません。
     */
    @Nullable
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

    public PluginsInstallArgument(@NotNull Path pluginPath, @NotNull PluginDescriptionFile pluginDescription, @Nullable KPMInformationFile kpmInformation, @NotNull List<DependencyElement> dependencies)
    {
        this.pluginPath = pluginPath;
        this.pluginDescription = pluginDescription;
        this.kpmInformation = kpmInformation;
        this.dependencies = dependencies;
    }

    public PluginsInstallArgument(@NotNull List<DependencyElement> dependencies)
    {
        this.pluginPath = null;
        this.pluginDescription = null;
        this.kpmInformation = null;
        this.dependencies = dependencies;
    }


}
