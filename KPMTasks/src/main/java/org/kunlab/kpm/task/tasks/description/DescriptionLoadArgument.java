package org.kunlab.kpm.task.tasks.description;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.task.interfaces.TaskArgument;

import java.nio.file.Path;

/**
 * プラグイン情報ファイルを読み込む際に使用される引数を表します。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class DescriptionLoadArgument implements TaskArgument
{
    /**
     * プラグイン情報ファイルのパスです。
     */
    @NotNull
    Path pluginFile;

    public DescriptionLoadArgument(@NotNull Path pluginFile)
    {
        this.pluginFile = pluginFile;
    }
}
