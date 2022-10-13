package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.resolve.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * プラグインが正常に解決されたことを示すシグナルです。
 */
@Data
public class PluginResolvedSuccessfulSignal implements Signal
{
    /**
     * 解決する際に使用されたクエリです。
     * 値を変更すると、解決されたプラグインが変更されます。
     */
    @NotNull
    private SuccessResult resolvedPlugin;
}
