package net.kunmc.lab.kpm.installer.impls.install;

import net.kunmc.lab.kpm.task.AbstractInstallTask;
import net.kunmc.lab.kpm.task.tasks.dependencies.collector.DependsCollectTask;
import net.kunmc.lab.kpm.task.tasks.dependencies.computer.signals.DependsLoadOrderComputingSignal;
import net.kunmc.lab.kpm.task.tasks.description.DescriptionLoadTask;
import net.kunmc.lab.kpm.task.tasks.download.DownloadTask;
import net.kunmc.lab.kpm.task.tasks.resolve.PluginResolveTask;

/**
 * インストールに必要なタスクをまとめた列挙型です。
 * デバッグ用に使用されることがあります。
 */
public enum InstallTasks
{
    /**
     * インストーラが初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * インストーラの初期化中であることを示します。
     */
    INITIALIZING,

    /**
     * プラグイン・クエリの解決中であることを示します。
     *
     * @see PluginResolveTask
     */
    RESOLVING_QUERY,
    /**
     * プラグインのダウンロード中であることを示します。
     *
     * @see DownloadTask
     */
    DOWNLOADING,
    /**
     * プラグイン情報の読み込み中であることを示します。
     *
     * @see org.bukkit.plugin.PluginDescriptionFile
     * @see DescriptionLoadTask
     */
    LOADING_PLUGIN_DESCRIPTION,
    /**
     * 環境の状態を確認中であることを示します。
     * 例えば、既にインストールされているプラグインを新規にインストールしようとした場合や、
     * 無視リストに含まれているプラグインをインストールしようとしていないかどうかを確認します。
     */
    CHECKING_ENVIRONMENT,
    /**
     * 古いプラグインの削除中であることを示します。
     * このタスクは必ずしも実行されるわけではなく、{@link net.kunmc.lab.kpm.installer.impls.install.signals.AlreadyInstalledPluginSignal} で {@link net.kunmc.lab.kpm.installer.impls.install.signals.AlreadyInstalledPluginSignal#setReplacePlugin(boolean)} に {@code true} が設定されてた場合にのみ実行されます。
     */
    REMOVING_OLD_PLUGIN,
    /**
     * 依存関係のインストール中であることを示します。
     *
     * @see DependsCollectTask
     */
    COLLECTING_DEPENDENCIES,
    /**
     * プラグインの読み込み順序を計算中であることを示します。
     *
     * @see DependsLoadOrderComputingSignal
     */
    COMPUTING_LOAD_ORDER,
    /**
     * プラグインのインストール中であることを示します。
     *
     * @see AbstractInstallTask
     */
    INSTALLING_PLUGINS,
}
