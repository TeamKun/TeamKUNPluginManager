package net.kunmc.lab.teamkunpluginmanager.installer.impls.install;

/**
 * インストールに必要なタスクをまとめた列挙型です。
 * デバッグ用に使用されることがあります。
 */
public enum InstallTasks
{
    /**
     * インストールが開始されたことを示します。
     */
    STARTED,
    /**
     * インストーラの初期化中であることを示します。
     */
    INITIALIZING,

    /**
     * プラグイン・クエリの解決中であることを示します。
     *
     * @see net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.resolve.PluginResolveTask
     */
    RESOLVING_QUERY,
    /**
     * プラグインのダウンロード中であることを示します。
     *
     * @see net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.download.DownloadTask
     */
    DOWNLOADING,
    /**
     * プラグイン情報の読み込み中であることを示します。
     *
     * @see org.bukkit.plugin.PluginDescriptionFile
     * @see net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.description.DescriptionLoadTask
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
     * このタスクは必ずしも実行されるわけではなく、{@link net.kunmc.lab.teamkunpluginmanager.installer.impls.install.signals.AlreadyInstalledPluginSignal} で {@link net.kunmc.lab.teamkunpluginmanager.installer.impls.install.signals.AlreadyInstalledPluginSignal#setReplacePlugin(boolean)} に {@code true} が設定されてた場合にのみ実行されます。
     */
    REMOVING_OLD_PLUGIN,
    /**
     * 依存関係のインストール中であることを示します。
     *
     * @see net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.dependencies.collector.DependsCollectTask
     */
    COLLECTING_DEPENDENCIES,
    /**
     * プラグインの読み込み順序を計算中であることを示します。
     *
     * @see net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.dependencies.computer.signals.DependsLoadOrderComputingSignal
     */
    COMPUTING_LOAD_ORDER,
    /**
     * プラグインのインストール中であることを示します。
     *
     * @see net.kunmc.lab.teamkunpluginmanager.installer.task.InstallTask
     */
    INSTALLING_PLUGINS,
}
