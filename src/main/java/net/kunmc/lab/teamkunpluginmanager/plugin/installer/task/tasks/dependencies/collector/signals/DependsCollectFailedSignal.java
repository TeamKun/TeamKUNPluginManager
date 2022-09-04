package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;

import java.util.List;

/**
 * 依存関係の取得に失敗したことを示すシグナルです。
 * 注意：このシグナルは {@link DependencyCollectFailedSignalBase} とは違い個別には送信されず、依存関係の解決タスク終了後に一度送信されます。
 */
@Value
public class DependsCollectFailedSignal implements InstallerSignal
{
    /**
     * 取得に失敗した依存関係の名前です。
     */
    List<String> collectFailedPlugins;
}
