package net.kunmc.lab.kpm;

import net.kunmc.lab.kpm.interfaces.alias.AliasProvider;
import net.kunmc.lab.kpm.interfaces.hook.HookExecutor;
import net.kunmc.lab.kpm.interfaces.kpminfo.KPMInfoManager;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaManager;

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
}
