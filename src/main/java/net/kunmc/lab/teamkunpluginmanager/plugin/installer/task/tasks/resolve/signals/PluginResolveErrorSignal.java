package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.signals;

import lombok.Data;
import lombok.NonNull;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ErrorResult;

/**
 * プラグインの解決に失敗したことを示すシグナルです。
 */
@Data
public class PluginResolveErrorSignal implements InstallerSignal
{
    /**
     * 失敗したプラグインの解決結果です。
     */
    @NonNull
    private final ErrorResult error;
}
