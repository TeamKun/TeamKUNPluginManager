package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 依存関係の依存関係を取得する際に失敗したことを示すシグナルです。
 */
@Value
public class DependencyCollectDependencysDependsFailedSignal implements InstallerSignal
{
    /**
     * 対象のプラグインの名前です。
     */
    @NotNull
    String pluginName;
    /**
     * 取得に失敗した依存関係の名前です。
     */
    @NotNull
    List<String> collectFailedDependencies;
}
