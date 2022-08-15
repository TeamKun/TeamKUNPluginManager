package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.description;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskResult;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

@Getter
public class DescriptionLoadResult extends TaskResult<DescriptionLoadState, DescriptionLoadErrorCause>
{
    @NotNull
    private final Path pluginFile;
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
