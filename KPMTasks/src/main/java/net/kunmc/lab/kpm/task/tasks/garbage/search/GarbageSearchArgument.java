package net.kunmc.lab.kpm.task.tasks.garbage.search;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.task.TaskArgument;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * 不要データ検索を行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class GarbageSearchArgument implements TaskArgument
{
    /**
     * 検索から除外するデータのリストです。
     */
    @NotNull
    List<String> excludes;

    /**
     * 検索するディレクトリのパスです。
     */
    @NotNull
    Path dataFolder;

    /**
     * サーバに存在するプラグインのリストです。
     */
    @NotNull
    List<String> plugins;
}
