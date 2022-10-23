package net.kunmc.lab.kpm.installer.task.tasks.description;

import lombok.Getter;
import net.kunmc.lab.kpm.installer.task.TaskResult;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * プラグイン情報ファイルの読み込み結果を表します。
 */
@Getter  // TODO: make @Value
public class DescriptionLoadResult extends TaskResult<DescriptionLoadState, DescriptionLoadErrorCause>
{
    /**
     * プラグインファイルの場所です。
     */
    @NotNull
    private final Path pluginFile;
    /**
     * 読み込まれた、プラグイン情報ファイルの概念的なオブジェクトです。
     */
    @Nullable
    private final PluginDescriptionFile description;

    public DescriptionLoadResult(boolean success, @NotNull DescriptionLoadState taskState,
                                 @Nullable DescriptionLoadErrorCause errorCause,
                                 @NotNull Path pluginFile, @Nullable PluginDescriptionFile description)
    {
        super(success, taskState, errorCause);

        this.pluginFile = pluginFile;
        this.description = description;
    }
}
