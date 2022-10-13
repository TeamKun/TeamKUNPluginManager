package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.description;

import net.kunmc.lab.teamkunpluginmanager.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.installer.task.InstallTask;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.description.signals.LoadPluginDescriptionSignal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandleManager;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

/**
 * プラグイン情報ファイルの読み込みを行うタスクです。
 */
public class DescriptionLoadTask extends InstallTask<DescriptionLoadArgument, DescriptionLoadResult>
{
    private DescriptionLoadState taskState;

    public DescriptionLoadTask(@NotNull InstallProgress<?, ?> progress, @NotNull SignalHandleManager signalHandler)
    {
        super(progress, signalHandler);
        this.taskState = DescriptionLoadState.INITIALIZED;
    }

    @Override
    public @NotNull DescriptionLoadResult runTask(@NotNull DescriptionLoadArgument arguments)
    {
        Path pluginFile = arguments.getPluginFile();

        this.postSignal(new LoadPluginDescriptionSignal(arguments.getPluginFile()));

        PluginDescriptionFile pluginYml;

        try
        {
            this.taskState = DescriptionLoadState.LOADING_PLUGIN_DESCRIPTION;
            pluginYml = PluginUtil.loadDescription(arguments.getPluginFile().toFile());
        }
        catch (InvalidDescriptionException e)
        {
            if (e.getMessage().equals("This file isn't plugin."))
                return new DescriptionLoadResult(false, this.taskState,
                        DescriptionLoadErrorCause.NOT_A_PLUGIN, pluginFile, null
                );
            else
            {
                e.printStackTrace();
                return new DescriptionLoadResult(false, this.taskState,
                        DescriptionLoadErrorCause.INVALID_DESCRIPTION, pluginFile, null
                );
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new DescriptionLoadResult(false, this.taskState,
                    DescriptionLoadErrorCause.IO_EXCEPTION, pluginFile, null
            );
        }

        return new DescriptionLoadResult(true, this.taskState, null, pluginFile, pluginYml);
    }
}
