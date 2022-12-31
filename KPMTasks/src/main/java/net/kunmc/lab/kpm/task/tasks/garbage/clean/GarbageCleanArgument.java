package net.kunmc.lab.kpm.task.tasks.garbage.clean;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.task.TaskArgument;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * 不要データ削除を行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class GarbageCleanArgument implements TaskArgument
{
    /**
     * 不要データとして削除するディレクトリ・ファイルのパスです。
     */
    @NotNull
    List<Path> paths;
}
