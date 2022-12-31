package net.kunmc.lab.kpm.installer.task.tasks.garbage.clean;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.installer.task.TaskArgument;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * 不要データ削除を行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class GarbageCleanArgument extends TaskArgument
{
    /**
     * 不要データとして削除するディレクトリ・ファイルのパスです。
     */
    @NotNull
    List<Path> paths;
}
