package org.kunlab.kpm.installer.impls.autoremove;

import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.AbstractInstaller;
import org.kunlab.kpm.installer.impls.autoremove.signals.PluginEnumeratedSignal;
import org.kunlab.kpm.installer.impls.autoremove.signals.PluginUninstallFailedSignal;
import org.kunlab.kpm.installer.impls.uninstall.PluginUninstallSucceedResult;
import org.kunlab.kpm.installer.impls.uninstall.PluginUninstaller;
import org.kunlab.kpm.installer.impls.uninstall.UnInstallTasks;
import org.kunlab.kpm.installer.impls.uninstall.UninstallArgument;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.signal.SignalHandleManager;
import org.kunlab.kpm.task.TaskFailedException;
import org.kunlab.kpm.task.tasks.uninstall.UnInstallResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * プラグインを自動削除するインストーラーの実装です。
 * 自動削除は主に以下の流れで動作します。
 *
 * <ol>
 *     <li>{@link AutoRemoveTasks#SEARCHING_REMOVABLES} - 自動削除対象のプラグインを検索する。</li>
 *     <li>{@link AutoRemoveTasks#UNINSTALLING_PLUGINS} - 自動削除対象のプラグインを削除する。</li>
 * </ol>
 */
public class PluginAutoRemover extends AbstractInstaller<AutoRemoveArgument, AutoRemoveErrorCause, AutoRemoveTasks>
{
    public PluginAutoRemover(@NotNull KPMRegistry registry, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(registry, signalHandler);
    }

    @Override
    public InstallResult<AutoRemoveTasks> execute(@NotNull AutoRemoveArgument argument) throws TaskFailedException
    {
        List<String> targetPlugins;
        // region Enumerate plugins to be removed
        this.progress.setCurrentTask(AutoRemoveTasks.SEARCHING_REMOVABLES);

        List<String> excludePlugins = argument.getExcludePlugins().stream().parallel()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        targetPlugins = this.registry.getPluginMetaManager().getProvider().getUnusedPlugins()
                .stream()
                .parallel()
                .filter(unusedPluginName -> !excludePlugins.contains(unusedPluginName.toLowerCase()))
                .collect(Collectors.toList());

        if (targetPlugins.isEmpty())
            return this.error(AutoRemoveErrorCause.NO_AUTO_REMOVABLE_PLUGIN_FOUND);

        PluginEnumeratedSignal signal = new PluginEnumeratedSignal(targetPlugins);
        this.postSignal(signal);
        if (signal.isCancel())
            return this.error(AutoRemoveErrorCause.CANCELLED);

        // Target plugins may be changed by signal handler.
        targetPlugins = new ArrayList<>(signal.getTargetPlugins());

        if (targetPlugins.isEmpty())
            return this.error(AutoRemoveErrorCause.NO_AUTO_REMOVABLE_PLUGIN_FOUND);

        // endregion

        // region Do uninstall
        this.progress.setCurrentTask(AutoRemoveTasks.UNINSTALLING_PLUGINS);

        PluginUninstaller uninstaller;
        try
        {
            uninstaller = new PluginUninstaller(this.registry, this.signalHandler);
        }
        catch (IOException e)
        {
            this.registry.getExceptionHandler().report(e);
            return this.error(AutoRemoveErrorCause.UNINSTALLER_INIT_FAILED);
        }

        UninstallArgument uninstallArgument = UninstallArgument.builder(targetPlugins.toArray(new String[0]))
                .build();

        InstallResult<UnInstallTasks> uninstallResult = uninstaller.execute(uninstallArgument);

        if (!uninstallResult.isSuccess())
        {
            this.postSignal(new PluginUninstallFailedSignal(uninstallResult));
            return this.error(AutoRemoveErrorCause.UNINSTALL_FAILED);
        }

        UnInstallResult unInstallResult = ((PluginUninstallSucceedResult) uninstallResult).getResult();
        List<PluginDescriptionFile> uninstalledPlugins = unInstallResult.getUninstalledPlugins();

        uninstalledPlugins.forEach(pluginDescription -> this.progress.addRemoved(pluginDescription, false));

        // endregion

        return this.success(new AutoRemoveSucceedResult(this.progress, unInstallResult));
    }
}
