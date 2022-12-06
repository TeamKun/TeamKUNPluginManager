package net.kunmc.lab.kpm.installer.task.tasks.uninstall;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.impls.uninstall.signals.PluginIsDependencySignal;
import net.kunmc.lab.kpm.installer.task.TaskArgument;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * プラグインのアンインストールを行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class UninstallArgument extends TaskArgument
{
    /**
     * アンインストールされるプラグインです。
     */
    List<Plugin> plugins;
    /**
     * アンインストールするプラグインの依存関係の名前です。
     * 依存関係の処理の分岐に使用されます。
     */
    @Nullable
    List<String> dependencies;

    /**
     * 無効だけを行うかどうかです。
     */
    boolean disableOnly;
    /**
     * ファイルを削除するかどうかです。
     */
    boolean deleteFile;
    /**
     * GC(ガベージ・コレクション)を行うかどうかです。
     */
    boolean runGC;

    /**
     * 依存関係の処理方法です。
     */
    @Nullable
    PluginIsDependencySignal.Operation operation;

    public UninstallArgument(List<Plugin> plugins, @Nullable List<String> dependencies)
    {
        this(plugins, dependencies, false, true, true, null);
    }

    public UninstallArgument(List<Plugin> plugins)
    {
        this(plugins, null, false, true, true, null);
    }

    public UninstallArgument(List<Plugin> plugins, @Nullable List<String> dependencies, boolean disableOnly, boolean deleteFile, boolean runGC)
    {
        this(plugins, dependencies, disableOnly, deleteFile, runGC, null);
    }

    public UninstallArgument(List<Plugin> plugins, boolean disableOnly, boolean deleteFile)
    {
        this(plugins, null, disableOnly, deleteFile, true, null);
    }

    public UninstallArgument(List<Plugin> plugins, @Nullable List<String> dependencies, @Nullable PluginIsDependencySignal.Operation operation)
    {
        this(plugins, dependencies, false, true, true, operation);
    }
}
