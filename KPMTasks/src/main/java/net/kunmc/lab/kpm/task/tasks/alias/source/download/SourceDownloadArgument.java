package net.kunmc.lab.kpm.task.tasks.alias.source.download;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.task.TaskArgument;

import java.util.Map;

/**
 * ソースファイルのダウンロードを行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class SourceDownloadArgument implements TaskArgument
{
    /**
     * エイリアスのソースのURLです。
     * キーはリモートの名前、値はHTTP(S)サーバのURLです。
     */
    Map<String, String> remotes;
}
