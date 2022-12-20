package net.kunmc.lab.kpm.installer.impls.upgrade;

/**
 * アップグレードのタスクを表す列挙型です。
 */
public enum UpgradeTasks
{
    /**
     * アップグレードが初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * アップグレード対象の検索中であることを示します。
     */
    SEARCHING_PLUGIN,
    /**
     * 環境の状態を確認中であることを示します。
     */
    CHECKING_ENVIRONMENT,
    /**
     * アップグレード対象のメタデータの取得中であることを示します。
     */
    RETRIEVING_METADATA,
    /**
     * アップグレード対象のクエリの取得中であることを示します。
     */
    RETRIEVING_UPDATE_QUERY,
    /**
     * プラグインの解決中であることを示します。
     */
    RESOLVING_PLUGIN,
    /**
     * プラグインのアンインストール中であることを示します。
     */
    UNINSTALLING_PLUGIN,
    /**
     * プラグインのインストール中であることを示します。
     */
    INSTALLING_PLUGIN,
    /**
     * 無効化した依存関係のプラグインの読み込み順の再設定中であることを示します。
     */
    COMPUTING_DEPENDENCY_LOAD_ORDER,
    /**
     * 無効化した依存関係のプラグインの再有効化中であることを示します。
     */
    RE_LOADING_DEPENDENCIES,
}
