package org.kunlab.kpm.task.tasks.resolve.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.resolver.result.ErrorResult;
import org.kunlab.kpm.signal.Signal;

/**
 * プラグインの解決に失敗したことを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginResolveErrorSignal extends Signal
{
    /**
     * 失敗したプラグインの解決結果です。
     */
    @NonNull
    private final ErrorResult error;
    /**
     * 解決しようとしたクエリです。
     */
    @NotNull
    private final String query;
}
