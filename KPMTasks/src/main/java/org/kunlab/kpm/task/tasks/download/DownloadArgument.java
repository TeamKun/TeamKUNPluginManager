package org.kunlab.kpm.task.tasks.download;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.task.TaskArgument;

import java.nio.file.Path;

/**
 * ダウンロードの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class DownloadArgument implements TaskArgument
{
    /**
     * ダウンロードするURLです。
     */
    @NotNull
    String url;

    /**
     * ダウンロード先のパスです。
     */
    @Nullable
    Path path;

    public DownloadArgument(@NotNull String url)
    {
        this.url = url;
        this.path = null;
    }

}
