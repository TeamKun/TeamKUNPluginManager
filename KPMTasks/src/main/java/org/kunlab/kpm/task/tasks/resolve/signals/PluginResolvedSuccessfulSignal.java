package org.kunlab.kpm.task.tasks.resolve.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.resolver.interfaces.result.SuccessResult;
import org.kunlab.kpm.signal.Signal;

/**
 * プラグインが正常に解決されたことを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginResolvedSuccessfulSignal extends Signal
{
    /**
     * 解決する際に使用されたクエリです。
     * 値を変更すると、解決されたプラグインが変更されます。
     */
    @NotNull
    private SuccessResult resolvedPlugin;
}
