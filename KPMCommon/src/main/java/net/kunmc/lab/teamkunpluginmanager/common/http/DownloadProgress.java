package net.kunmc.lab.teamkunpluginmanager.common.http;

import lombok.Value;

/**
 * ダウンロードの進捗を表すクラスです。
 */
@Value
public class DownloadProgress
{
    /**
     * ファイルのサイズです。
     */
    long totalSize;
    /**
     * ダウンロード済みのサイズです。
     */
    long downloaded;

    /**
     * ダウンロード済みのサイズをパーセントで表した値です。
     */
    double percentage;
}
