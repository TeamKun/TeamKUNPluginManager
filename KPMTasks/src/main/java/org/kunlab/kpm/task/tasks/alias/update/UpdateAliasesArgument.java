package org.kunlab.kpm.task.tasks.alias.update;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.kunlab.kpm.task.interfaces.TaskArgument;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;

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
    Map<String, Pair<URI, Path>> sources;
}
