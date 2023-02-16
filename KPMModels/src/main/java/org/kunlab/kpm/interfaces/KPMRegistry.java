package org.kunlab.kpm.interfaces;

import org.kunlab.kpm.TokenStore;
import org.kunlab.kpm.interfaces.alias.AliasProvider;
import org.kunlab.kpm.interfaces.hook.HookExecutor;
import org.kunlab.kpm.interfaces.installer.InstallManager;
import org.kunlab.kpm.interfaces.installer.loader.PluginLoader;
import org.kunlab.kpm.interfaces.kpminfo.KPMInfoManager;
import org.kunlab.kpm.interfaces.meta.PluginMetaManager;
import org.kunlab.kpm.interfaces.resolver.PluginResolver;
import org.kunlab.kpm.utils.ServerConditionChecker;
import org.kunlab.kpm.versioning.Version;

import java.util.logging.Logger;

/**
 * KPM のモジュールのレジストリです。
 */
public interface KPMRegistry
{
    /**
     * ロガーを取得します。
     */
    Logger getLogger();

    /**
     * デーモンのバージョンを取得します。
     */
    Version getVersion();

    /**
     * KPM 環境を取得します。
     */
    KPMEnvironment getEnvironment();

    /**
     * エイリアスを管理するクラスです。
     */
    AliasProvider getAliasProvider();

    /**
     * プラグインのメタデータを管理するクラスです。
     */
    PluginMetaManager getPluginMetaManager();

    /**
     * プラグインのKPM情報ファイルを管理するクラスです。
     */
    KPMInfoManager getKpmInfoManager();

    /**
     * フックを実行するクラスです。
     */
    HookExecutor getHookExecutor();

    /**
     * トークンを保管しセキュアに管理するクラスです。
     */
    TokenStore getTokenStore();

    /**
     * インストールを管理するクラスです。
     */
    InstallManager getInstallManager();

    /**
     * プラグインをロード/アンロードするためのクラスです。
     */
    PluginLoader getPluginLoader();

    /**
     * プラグインクエリを解決するクラスです。
     */
    PluginResolver getPluginResolver();

    /**
     * サーバの状態を取得するクラスです。
     */
    ServerConditionChecker getServerConditionChecker();

    /**
     * デーモンを終了します。
     */
    void shutdown();
}
