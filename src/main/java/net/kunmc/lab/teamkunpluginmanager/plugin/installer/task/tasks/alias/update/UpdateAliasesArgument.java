package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.alias.update;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskArgument;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * エイリアスのアップデートを行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class UpdateAliasesArgument extends TaskArgument
{
    /**
     * エイリアスのソースファイルの名前とペアのマップです。
     * ペアの左辺はリモートのURL、右辺はローカルのパスです。
     */
    HashMap<String, Pair<URL, Path>> sources;
}
