package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * ダウンロードの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class DownloadArgument extends TaskArgument
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

    public DownloadArgument(@NotNull String url, @Nullable Path path)
    {
        this.url = url;
        this.path = path;
    }

    public DownloadArgument(@NotNull String url)
    {
        this.url = url;
        this.path = null;
    }

}
