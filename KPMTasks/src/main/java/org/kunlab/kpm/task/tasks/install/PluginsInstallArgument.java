package org.kunlab.kpm.task.tasks.install;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.task.TaskArgument;
import org.kunlab.kpm.interfaces.task.tasks.dependencies.DependencyElement;
import org.kunlab.kpm.kpminfo.KPMInformationFile;

import java.nio.file.Path;
import java.util.List;

/**
 * プラグインのインストール時に渡される引数です。
 */
@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PluginsInstallArgument implements TaskArgument
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

    /**
     * プラグインの配置のみのモードです。
     * このフラグを {@code true} にした場合, プラグインの読み込みは行われません。
     */
    boolean onlyLocate;

    public PluginsInstallArgument(@NotNull List<DependencyElement> dependencies)
    {
        this(null, null, null, dependencies, false);
    }
}
