package net.kunmc.lab.kpm;

import net.kunmc.lab.kpm.interfaces.alias.AliasProvider;
import net.kunmc.lab.kpm.interfaces.hook.HookExecutor;
import net.kunmc.lab.kpm.interfaces.installer.InstallManager;
import net.kunmc.lab.kpm.interfaces.installer.loader.PluginLoader;
import net.kunmc.lab.kpm.interfaces.kpminfo.KPMInfoManager;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaManager;
import net.kunmc.lab.kpm.interfaces.resolver.PluginResolver;
import net.kunmc.lab.kpm.versioning.Version;

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
}
