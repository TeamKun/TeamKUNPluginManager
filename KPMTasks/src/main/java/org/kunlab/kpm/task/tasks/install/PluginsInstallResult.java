package org.kunlab.kpm.task.tasks.install;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.task.AbstractTaskResult;

import java.util.ArrayList;
import java.util.List;

/**
 * プラグインのインストール結果を表すクラスです。
 */
public class PluginsInstallResult extends AbstractTaskResult<PluginsInstallState, PluginsInstallErrorCause>
{
    /**
     * インストールに失敗した場合、そのプラグインの名前が格納されます。
     */
    @Getter
    @Nullable
    private final String failedPluginName;

    /**
     * インストールに成功した場合、そのプラグインが格納されます。
     */
    @Getter
    @Nullable
    private final Plugin installedPlugin;
    /**
     * インストールされたプラグインの依存関係が格納されます。
     */
    @Getter
    @NotNull
    private final List<Plugin> collectedDependencies;

    public PluginsInstallResult(boolean success, @NotNull PluginsInstallState state, @Nullable PluginsInstallErrorCause errorCause)
    {
        this(success, state, errorCause, null, null, new ArrayList<>());
    }

    public PluginsInstallResult(boolean success, @NotNull PluginsInstallState state, @Nullable PluginsInstallErrorCause errorCause, @Nullable String failedPluginName, @Nullable Plugin installedPlugin, @NotNull List<Plugin> collectedDependencies)
    {
        super(success, state, errorCause);
        this.failedPluginName = failedPluginName;
        this.installedPlugin = installedPlugin;
        this.collectedDependencies = collectedDependencies;
    }
}
