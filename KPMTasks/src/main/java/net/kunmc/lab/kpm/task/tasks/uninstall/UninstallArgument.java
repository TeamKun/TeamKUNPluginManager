package net.kunmc.lab.kpm.task.tasks.uninstall;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.interfaces.task.TaskArgument;
import net.kunmc.lab.kpm.task.tasks.uninstall.signals.PluginIsDependencySignal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * プラグインのアンインストールを行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class UninstallArgument implements TaskArgument
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

    public UninstallArgument(Plugin plugin)
    {
        this(Collections.singletonList(plugin), null, false, true, true, null);
    }

    public UninstallArgument(Plugin plugin, @Nullable List<String> dependencies)
    {
        this(Collections.singletonList(plugin), dependencies, false, true, true, null);
    }

    public UninstallArgument(Plugin plugin, @Nullable List<String> dependencies, boolean disableOnly, boolean deleteFile, boolean runGC)
    {
        this(Collections.singletonList(plugin), dependencies, disableOnly, deleteFile, runGC, null);
    }

    public UninstallArgument(Plugin plugin, boolean disableOnly, boolean deleteFile)
    {
        this(Collections.singletonList(plugin), null, disableOnly, deleteFile, true, null);
    }

    public UninstallArgument(Plugin plugin, @Nullable List<String> dependencies, @Nullable PluginIsDependencySignal.Operation operation)
    {
        this(Collections.singletonList(plugin), dependencies, false, true, true, operation);
    }
}

