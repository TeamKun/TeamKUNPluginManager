package org.kunlab.kpm.task.tasks.description;

import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.installer.interfaces.PluginInstaller;
import org.kunlab.kpm.task.AbstractInstallTask;
import org.kunlab.kpm.task.tasks.description.signals.LoadPluginDescriptionSignal;
import org.kunlab.kpm.utils.PluginUtil;

import java.io.IOException;
import java.nio.file.Path;

/**
 * プラグイン情報ファイルの読み込みを行うタスクです。
 */
public class DescriptionLoadTask extends AbstractInstallTask<DescriptionLoadArgument, DescriptionLoadResult>
{
    private DescriptionLoadState taskState;

    public DescriptionLoadTask(@NotNull PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());
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
                this.progress.getInstaller().getRegistry().getExceptionHandler().report(e);
                return new DescriptionLoadResult(false, this.taskState,
                        DescriptionLoadErrorCause.INVALID_DESCRIPTION, pluginFile, null
                );
            }
        }
        catch (IOException e)
        {
            this.progress.getInstaller().getRegistry().getExceptionHandler().report(e);
            return new DescriptionLoadResult(false, this.taskState,
                    DescriptionLoadErrorCause.IO_EXCEPTION, pluginFile, null
            );
        }

        return new DescriptionLoadResult(true, this.taskState, null, pluginFile, pluginYml);
    }
}
