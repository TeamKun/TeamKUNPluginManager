package net.kunmc.lab.kpm.task.tasks.download;

/**
 * ダウンロードの状態を表します。
 */
public enum DownloadState
{
    /**
     * ダウンロードが初期化されたことを示します。
     */
    INITIALIZED,

    /**
     * ダウンロードが開始中であることを示します。
     */
    START_DOWNLOADING,
    /**
     * ダウンロード中であることを示します。
     */
    DOWNLOADING,
}
