package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.description;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.DownloadResult;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * プラグイン情報ファイルを読み込む際に使用される引数を表します。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class DescriptionLoadArgument extends TaskArgument
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

    public DescriptionLoadArgument(DownloadResult previousTaskResult)
    {
        super(previousTaskResult);

        if (previousTaskResult.getPath() == null)
            throw new IllegalArgumentException("DownloadResult.path is null");

        this.pluginFile = previousTaskResult.getPath();
    }
}
