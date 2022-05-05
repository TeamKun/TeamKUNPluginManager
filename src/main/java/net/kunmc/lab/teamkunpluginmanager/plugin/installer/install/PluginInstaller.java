package net.kunmc.lab.teamkunpluginmanager.plugin.installer.install;

import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.DescriptionLoadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolvePhase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PluginInstaller extends AbstractInstaller<InstallErrorCause, InstallPhases>
{
    public PluginInstaller(@NotNull InstallerSignalHandler signalHandler) throws IOException
    {
        super(signalHandler);
    }

    public InstallResult<InstallPhases> execute(@NotNull String query)
    {
        DescriptionLoadResult pluginDescriptionResult = (DescriptionLoadResult)
                this.submitter(InstallPhases.QUERY_RESOLVING, new PluginResolvePhase(progress, signalHandler))
                        .then(InstallPhases.DOWNLOADING, new DownloadPhase(progress, signalHandler))
                        .then(InstallPhases.PLUGIN_DESCRIPTION_LOADING, new DescriptionLoadPhase(progress, signalHandler))
                        .submit(new PluginResolveArgument(query));

        if (!pluginDescriptionResult.isSuccess())
            return handlePhaseError(pluginDescriptionResult);

    }
}
