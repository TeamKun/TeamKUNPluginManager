package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadResult;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Value
@EqualsAndHashCode(callSuper = false)
public class DescriptionLoadArgument extends PhaseArgument
{
    @NotNull
    Path pluginFile;

    public DescriptionLoadArgument(@NotNull Path pluginFile)
    {
        this.pluginFile = pluginFile;
    }

    public DescriptionLoadArgument(DownloadResult previousPhaseResult)
    {
        super(previousPhaseResult);

        if (previousPhaseResult.getPath() == null)
            throw new IllegalArgumentException("DownloadResult.path is null");

        this.pluginFile = previousPhaseResult.getPath();
    }
}
