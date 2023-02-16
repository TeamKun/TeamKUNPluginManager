package org.kunlab.kpm.interfaces.resolver.result;

/**
 * 解決に成功したことを表すクエリ解決結果です。
 */
public interface SuccessResult extends ResolveResult
{
    /**
     * プラグインのダウンロードリンクです。
     */
    String getDownloadUrl();

    /**
     * プラグインのファイル名です。
     */
    String getFileName();

    /**
     * プラグインのバージョンです。
     */
    String getVersion();
}
