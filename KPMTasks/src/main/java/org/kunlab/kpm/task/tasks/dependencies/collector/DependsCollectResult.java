package org.kunlab.kpm.task.tasks.dependencies.collector;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.task.AbstractTaskResult;
import org.kunlab.kpm.task.interfaces.dependencies.DependencyElement;

import java.util.List;

/**
 * 依存関係の解決結果を表します。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class DependsCollectResult extends AbstractTaskResult<DependsCollectState, DependsCollectErrorCause>
{
    /**
     * 依存関係解決対象のプラグインです。
     */
    @NotNull
    String targetPlugin;

    /**
     * 依存関係の要素のリストです。解決に失敗した場合は空のリストです。
     *
     * @see #isSuccess()
     */
    @NotNull
    List<DependencyElement> collectedPlugins;

    /**
     * 解決に失敗した依存関係の要素のリストです。解決に成功した場合は空のリストです。
     *
     * @see #isSuccess()
     */
    @NotNull
    List<String> collectFailedPlugins;

    public DependsCollectResult(@NotNull DependsCollectState taskState,
                                @Nullable DependsCollectErrorCause errorCause, @NotNull String targetPlugin,
                                @NotNull List<DependencyElement> collectedPlugins,
                                @NotNull List<String> collectFailedPlugins)
    {
        super(collectFailedPlugins.isEmpty(), taskState, errorCause);
        this.targetPlugin = targetPlugin;
        this.collectedPlugins = collectedPlugins;
        this.collectFailedPlugins = collectFailedPlugins;
    }
}
