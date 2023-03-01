package org.kunlab.kpm.task.tasks.garbage.clean;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.task.interfaces.TaskArgument;

import java.nio.file.Path;
import java.util.List;

/**
 * 不要なデータの削除を行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class GarbageCleanArgument implements TaskArgument
{
    /**
     * 不要なデータとして削除するディレクトリ・ファイルのパスです。
     */
    @NotNull
    List<Path> paths;
}
