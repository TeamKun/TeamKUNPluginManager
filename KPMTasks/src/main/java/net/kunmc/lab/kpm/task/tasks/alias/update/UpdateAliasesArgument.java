package net.kunmc.lab.kpm.task.tasks.alias.update;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.task.TaskArgument;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * エイリアスのアップデートを行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class UpdateAliasesArgument implements TaskArgument
{
    /**
     * エイリアスのソースファイルの名前とペアのマップです。
     * ペアの左辺はリモートのURL、右辺はローカルのパスです。
     */
    HashMap<String, Pair<URI, Path>> sources;
}
