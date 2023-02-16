package org.kunlab.kpm.task.tasks.dependencies.computer;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.task.tasks.dependencies.DependencyElement;
import org.kunlab.kpm.task.AbstractTaskResult;

import java.util.List;

/**
 * 依存関係の読み込み順序を計算した結果を表します。
 */
public class DependsComputeOrderResult extends AbstractTaskResult<DependsComputeOrderState, DependsComputeOrderErrorCause>
{
    /**
     * 計算された依存関係の読み込み順序リストです。
     */
    @Getter
    @NotNull
    private final List<DependencyElement> order;

    public DependsComputeOrderResult(boolean success, @NotNull DependsComputeOrderState taskState,
                                     @Nullable DependsComputeOrderErrorCause errorCause,
                                     @NotNull List<DependencyElement> order)
    {
        super(success, taskState, errorCause);
        this.order = order;
    }
}
