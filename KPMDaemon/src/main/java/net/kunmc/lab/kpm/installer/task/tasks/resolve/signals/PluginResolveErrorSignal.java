package net.kunmc.lab.kpm.installer.task.tasks.resolve.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

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
