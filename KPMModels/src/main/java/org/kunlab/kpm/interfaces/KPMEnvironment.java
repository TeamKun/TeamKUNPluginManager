package org.kunlab.kpm.interfaces;

import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * KPM の実行環境を表すクラスです。
 */
public interface KPMEnvironment
{
    /**
     * KPMのプラグインです。
     */
    Plugin getPlugin();

    /**
     * プラグインのデータディレクトリのパスです。
     */
    Path getDataDirPath();

    /**
     * KPMデーモンが使用するロガーです。
     */
    Logger getLogger();

    /**
     * トークンの格納先のパスです。
     */
    Path getTokenPath();

    /**
     * トークンの鍵の格納先のパスです。
     */
    Path getTokenKeyPath();

    /**
     * プラグインメタデータデータベースのパスです。
     */
    Path getMetadataDBPath();

    /**
     * エイリアスデータベースのパスです。
     */
    Path getAliasesDBPath();

    /**
     * プラグイン解決に使用するGitHubの組織名です。
     */
    List<String> getOrganizations();

    /**
     * 様々な操作から除外するプラグインの名前です。
     * 通常は、削除やアップデートを行わないようにするために使用します。
     */
    List<String> getExcludes();

    /**
     * エイリアスのソースです。
     */
    Map<String, String> getSources();

    /**
     * HTTP リクエストのユーザーエージェントです。
     */
    String getHTTPUserAgent();

    /**
     * HTTP リクエストのタイムアウト時間です。
     */
    int getHTTPTimeout();

    /**
     * HTTP リクエストのリダイレクト回数です。
     */
    int getHTTPMaxRedirects();
}
