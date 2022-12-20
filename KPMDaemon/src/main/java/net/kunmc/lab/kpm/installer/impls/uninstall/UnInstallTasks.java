package net.kunmc.lab.kpm.installer.impls.uninstall;

/**
 * アンインストールのタスクを表す列挙型です。
 */
public enum UnInstallTasks
{
    /**
     * アンインストーラが初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * プラグインの検索中を示します。
     */
    SEARCHING_PLUGIN,
    /**
     * 環境の状態の確認中を示します。
     * 例えば、プラグインが存在するかや、無視リストに登録されていないかなどを確認します。
     */
    CHECKING_ENVIRONMENT,

    /**
     * アンインストール順序の計算中を示します。
     */
    COMPUTING_UNINSTALL_ORDER,
    /**
     * プラグインをアンインストール中を示します。
     */
    UNINSTALLING_PLUGINS,
}
