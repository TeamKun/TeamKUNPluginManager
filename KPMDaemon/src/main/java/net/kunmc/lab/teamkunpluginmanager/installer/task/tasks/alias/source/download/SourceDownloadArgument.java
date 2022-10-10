package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.alias.source.download;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.installer.task.TaskArgument;

import java.util.Map;

/**
 * ソースファイルのダウンロードを行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class SourceDownloadArgument extends TaskArgument
{
    /**
     * エイリアスのソースのURLです。
     * キーはリモートの名前、値はHTTP(S)サーバのURLです。
     */
    Map<String, String> remotes;
}
