package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.InstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.description.signals.LoadPluginDescriptionSignal;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DescriptionLoadPhase extends InstallPhase<DescriptionLoadArgument, DescriptionLoadResult>
{
    private DescriptionLoadState phaseState;

    public DescriptionLoadPhase(@NotNull InstallProgress<?> progress, @NotNull InstallerSignalHandler signalHandler)
    {
        super(progress, signalHandler);
        this.phaseState = DescriptionLoadState.INITIALIZED;
    }

    @Override
    public @NotNull DescriptionLoadResult runPhase(@NotNull DescriptionLoadArgument arguments)
    {
        this.postSignal(new LoadPluginDescriptionSignal(arguments.getPluginFile()));

        PluginDescriptionFile pluginYml;

        try
        {
            this.phaseState = DescriptionLoadState.LOADING_PLUGIN_DESCRIPTION;
            pluginYml = PluginUtil.loadDescription(arguments.getPluginFile().toFile());
        }
        catch (InvalidDescriptionException e)
        {
            if (e.getMessage().equals("This file isn't plugin."))
                return new DescriptionLoadResult(false, this.phaseState,
                        DescriptionLoadErrorCause.NOT_A_PLUGIN, null
                );
            else
            {
                e.printStackTrace();
                return new DescriptionLoadResult(false, this.phaseState,
                        DescriptionLoadErrorCause.INVALID_DESCRIPTION, null
                );
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new DescriptionLoadResult(false, this.phaseState,
                    DescriptionLoadErrorCause.IO_EXCEPTION, null
            );
        }

        return new DescriptionLoadResult(true, this.phaseState, null, pluginYml);
    }
}
