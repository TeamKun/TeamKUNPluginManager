package org.kunlab.kpm.task.tasks.resolve;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.resolver.interfaces.result.SuccessResult;
import org.kunlab.kpm.task.AbstractTaskResult;

/**
 * プラグインの解決を行うタスクの結果です。
 */
public class PluginResolveResult extends AbstractTaskResult<PluginResolveState, PluginResolveErrorCause>
{
    /**
     * 解決されたプラグインの解決結果です。
     */
    @Getter
    @Nullable
    private final SuccessResult resolveResult;

    public PluginResolveResult(boolean success, @NotNull PluginResolveState taskState,
                               @NotNull PluginResolveErrorCause errorCause, @Nullable SuccessResult resolveResult)
    {
        super(success, taskState, errorCause);
        this.resolveResult = resolveResult;
    }

    public PluginResolveResult(boolean success, @NotNull PluginResolveState taskState, @NotNull SuccessResult resolveResult)
    {
        super(success, taskState, null);
        this.resolveResult = resolveResult;
    }

}
