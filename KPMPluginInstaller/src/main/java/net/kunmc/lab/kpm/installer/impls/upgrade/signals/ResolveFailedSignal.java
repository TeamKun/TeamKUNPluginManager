package net.kunmc.lab.kpm.installer.impls.upgrade.signals;

import lombok.Getter;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.PluginResolveErrorCause;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.PluginResolveState;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * 依存関係の解決に失敗したことを示すシグナルです。
 */
@Getter
public class ResolveFailedSignal extends PluginNotFoundSignal
{
    /**
     * 依存関係の解決に失敗したプラグインです。
     */
    @NotNull
    private final Plugin plugin;

    /**
     * 依存関係の解決に失敗した理由です。
     */
    @NotNull
    private final PluginResolveErrorCause errorCause;

    /**
     * 依存関係の解決に失敗したときの状態です。
     */
    @NotNull
    private final PluginResolveState resolveStateState;

    public ResolveFailedSignal(@NotNull Plugin plugin, @NotNull PluginResolveErrorCause errorCause, @NotNull PluginResolveState resolveStateState)
    {
        super(plugin.getDescription().getName());
        this.plugin = plugin;
        this.errorCause = errorCause;
        this.resolveStateState = resolveStateState;
    }
}
