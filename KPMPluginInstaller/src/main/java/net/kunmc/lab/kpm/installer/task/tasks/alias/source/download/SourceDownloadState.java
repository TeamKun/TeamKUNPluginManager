package net.kunmc.lab.kpm.installer.task.tasks.alias.source.download;

/**
 * ソースファイルのダウンロードの状態を表します。
 */
public enum SourceDownloadState
{
    /**
     * ソースファイルのダウンロードが初期化されたことを示します。
     */
    INITIALIZED,
    /**
     * URLの解析中であることを示します。
     */
    ANALYZING_URLS,
    /**
     * ソースをダウンロード中であることを示します。
     */
    DOWNLOADING_SOURCES,
}
