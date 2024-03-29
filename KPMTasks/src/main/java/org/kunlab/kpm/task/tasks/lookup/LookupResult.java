package org.kunlab.kpm.task.tasks.lookup;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.task.AbstractTaskResult;

import java.util.Map;

/**
 * プラグインの検索を行うタスクの結果です。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class LookupResult extends AbstractTaskResult<LookupState, LookupErrorCause>
{
    /**
     * 検索に成功した場合、そのプラグインらが格納されます。
     * {@link #isSuccess()}がfalseの場合や、クエリが間違っている場合などにこの値はnullになります。
     */
    @Nullable
    Map<@NotNull String, @Nullable Plugin> plugins;

    public LookupResult(boolean success, @NotNull LookupState state, @NotNull Map<String, Plugin> plugins)
    {
        super(success, state, null);
        this.plugins = plugins;
    }

    public LookupResult(boolean success, @NotNull LookupState state, @Nullable LookupErrorCause errorCause)
    {
        super(success, state, errorCause);
        this.plugins = null;
    }
}
