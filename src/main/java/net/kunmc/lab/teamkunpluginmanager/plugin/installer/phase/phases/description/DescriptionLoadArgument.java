package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadResult;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Value
public class DescriptionLoadArgument implements PhaseArgument
{
    @NotNull
    Path pluginFile;

    public static DescriptionLoadArgument of(@NotNull DownloadResult of)
    {
        if (!of.isSuccess() || of.getPath() == null)
            throw new IllegalArgumentException("Download must be successful");

        return new DescriptionLoadArgument(of.getPath());
    }
}
