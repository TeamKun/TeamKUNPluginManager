package org.kunlab.kpm.task.tasks.alias.update;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.task.AbstractTaskResult;

import java.util.Map;

/**
 * エイリアスのアップデートを行うタスクの結果です。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class UpdateAliasesResult extends AbstractTaskResult<UpdateAliasesState, UpdateAliasesErrorCause>
{
    boolean warn;
    long aliasesCount;
    Map<String, Long> aliasesCountBySource;

    public UpdateAliasesResult(boolean success, @NotNull UpdateAliasesState state, @Nullable UpdateAliasesErrorCause errorCause,
                               boolean warn, long aliasesCount, Map<String, Long> aliasesCountBySource)
    {
        super(success, state, errorCause);
        this.warn = warn;
        this.aliasesCount = aliasesCount;
        this.aliasesCountBySource = aliasesCountBySource;
    }

    public UpdateAliasesResult(boolean success, @NotNull UpdateAliasesState state,
                               boolean warn, long aliasesCount, Map<String, Long> aliasesCountBySource)
    {
        this(success, state, null, warn, aliasesCount, aliasesCountBySource);
    }
}
