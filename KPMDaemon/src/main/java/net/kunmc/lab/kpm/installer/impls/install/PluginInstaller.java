package net.kunmc.lab.kpm.installer.impls.install;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.installer.AbstractInstaller;
import net.kunmc.lab.kpm.installer.InstallResult;
import net.kunmc.lab.kpm.installer.impls.install.signals.AlreadyInstalledPluginSignal;
import net.kunmc.lab.kpm.installer.signals.InvalidKPMInfoFileSignal;
import net.kunmc.lab.kpm.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.kpm.installer.task.TaskFailedException;
import net.kunmc.lab.kpm.installer.task.TaskResult;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.collector.DependsCollectArgument;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.collector.DependsCollectTask;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.computer.DependsComputeOrderArgument;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.computer.DependsComputeOrderTask;
import net.kunmc.lab.kpm.installer.task.tasks.description.DescriptionLoadArgument;
import net.kunmc.lab.kpm.installer.task.tasks.description.DescriptionLoadResult;
import net.kunmc.lab.kpm.installer.task.tasks.description.DescriptionLoadTask;
import net.kunmc.lab.kpm.installer.task.tasks.download.DownloadArgument;
import net.kunmc.lab.kpm.installer.task.tasks.download.DownloadTask;
import net.kunmc.lab.kpm.installer.task.tasks.install.PluginsInstallArgument;
import net.kunmc.lab.kpm.installer.task.tasks.install.PluginsInstallTask;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.PluginResolveArgument;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.PluginResolveTask;
import net.kunmc.lab.kpm.kpminfo.InvalidInformationFileException;
import net.kunmc.lab.kpm.kpminfo.KPMInformationFile;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.utils.PluginUtil;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * プラグインを新規にインストールするインストーラーの実装です。
 * インストーラは主に以下の流れで動作します。
 * <ol>
 *     <li>クエリを解決する({@link InstallTasks#RESOLVING_QUERY})。</li>
 *     <li>プラグインをダウンロードする({@link InstallTasks#DOWNLOADING})。</li>
 *     <li>プラグイン情報ファイルを読み込む({@link InstallTasks#LOADING_PLUGIN_DESCRIPTION})。</li>
 *     <li>環境が適合しているかどうかをチェックする({@link InstallTasks#CHECKING_ENVIRONMENT})。</li>
 *     <li>依存関係を解決する({@link InstallTasks#COLLECTING_DEPENDENCIES})。</li>
 *     <li>インストール順序を計算する({@link InstallTasks#COMPUTING_LOAD_ORDER})。</li>
 *     <li>依存関係とプラグインをインストールする({@link InstallTasks#INSTALLING_PLUGINS})。</li>
 * </ol>
 */
public class PluginInstaller extends AbstractInstaller<InstallArgument, InstallErrorCause, InstallTasks>
{
    public PluginInstaller(@NotNull KPMDaemon daemon, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(daemon, signalHandler);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public InstallResult<InstallTasks> execute(@NotNull InstallArgument argument) throws TaskFailedException
    {
        String query = argument.getQuery();

        Path pluginFilePath;
        PluginDescriptionFile pluginDescription;
        String pluginName;
        // region Do plugin resolve, download and description load.

        TaskResult pluginDescriptionResult =
                this.submitter(
                                InstallTasks.RESOLVING_QUERY,
                                new PluginResolveTask(this)
                        )
                        .then(InstallTasks.DOWNLOADING, new DownloadTask(this))
                        .bridgeArgument(result -> {
                            if (result.getResolveResult() == null)
                                throw new IllegalArgumentException("Plugin Resolving must be successful");

                            return new DownloadArgument(result.getResolveResult().getDownloadUrl());
                        })
                        .then(InstallTasks.LOADING_PLUGIN_DESCRIPTION, new DescriptionLoadTask(this))
                        .bridgeArgument(result -> new DescriptionLoadArgument(result.getPath()))
                        .submitAll(new PluginResolveArgument(query));

        DescriptionLoadResult descriptionLoadResult = (DescriptionLoadResult) pluginDescriptionResult;

        pluginFilePath = descriptionLoadResult.getPluginFile();
        pluginDescription = descriptionLoadResult.getDescription();
        assert pluginDescription != null; // Not null because isSuccess() is true.

        pluginName = pluginDescription.getName();
        // endregion

        // Load kpmInfo
        KPMInformationFile kpmInfo = null;
        try
        {
            kpmInfo = this.daemon.getKpmInfoManager().loadInfo(pluginFilePath, pluginDescription);
        }
        catch (InvalidInformationFileException e)
        {
            InvalidKPMInfoFileSignal signal = new InvalidKPMInfoFileSignal(pluginFilePath, pluginDescription);
            this.postSignal(signal);
            if (!signal.isIgnore())
                return this.error(InstallErrorCause.INVALID_KPM_INFO_FILE);
        }
        catch (FileNotFoundException ignored)
        {
        }  // KPM info file is not required so we can ignore it.

        boolean replacePlugin = false;
        // region Do assertions.

        this.progress.setCurrentTask(InstallTasks.CHECKING_ENVIRONMENT);

        InstallErrorCause checkEnvErrorResult = this.checkEnvironment(pluginDescription);
        if (checkEnvErrorResult != null)
            return this.error(checkEnvErrorResult);

        // region Check if plugin is already installed.

        Plugin sameServerPlugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if (sameServerPlugin != null)
        {
            AlreadyInstalledPluginSignal alreadyInstalledPluginSignal =
                    new AlreadyInstalledPluginSignal(sameServerPlugin.getDescription(), pluginDescription);

            this.postSignal(alreadyInstalledPluginSignal);
            replacePlugin = alreadyInstalledPluginSignal.isReplacePlugin();

            if (!replacePlugin)
                return this.error(InstallErrorCause.PLUGIN_ALREADY_INSTALLED);
        }

        // endregion

        // endregion

        // region Remove plugin if it is already installed. (only replacePlugin is true)
        if (replacePlugin)
            this.removeOldPlugin(sameServerPlugin);
        // endregion

        this.progress.addPending(pluginDescription.getName());


        // region Do collect dependencies, compute dependencies load order and install them.
        KPMInformationFile finalKpmInfo = kpmInfo;
        TaskResult installResult =
                this.submitter(
                                InstallTasks.COLLECTING_DEPENDENCIES,
                                new DependsCollectTask(this)
                        )
                        .then(InstallTasks.COMPUTING_LOAD_ORDER, new DependsComputeOrderTask(this))
                        .bridgeArgument(result -> new DependsComputeOrderArgument(result.getCollectedPlugins()))
                        .then(
                                InstallTasks.INSTALLING_PLUGINS,
                                new PluginsInstallTask(this)
                        )
                        .bridgeArgument(result -> new PluginsInstallArgument(
                                pluginFilePath, pluginDescription, finalKpmInfo, result.getOrder()
                        ))
                        .submitAll(new DependsCollectArgument(pluginDescription));
        // endregion

        if (replacePlugin)
            this.progress.addUpgraded(pluginDescription);
        else
            this.progress.addInstalled(pluginDescription);

        return this.success();
    }

    @Nullable
    private InstallErrorCause checkEnvironment(PluginDescriptionFile pluginDescription)
    {
        String pluginName = pluginDescription.getName();


        // region Check if the plugin is marked as ignored
        if (this.isPluginIgnored(pluginName))
        {
            IgnoredPluginSignal ignoredPluginSignal = new IgnoredPluginSignal(pluginDescription);
            this.postSignal(ignoredPluginSignal);

            if (ignoredPluginSignal.isCancelInstall())
                return InstallErrorCause.PLUGIN_IGNORED;
        }
        // endregion

        return null;  // No error with environment.
    }

    private void removeOldPlugin(Plugin plugin)
    {
        this.progress.setCurrentTask(InstallTasks.REMOVING_OLD_PLUGIN);

        File oldPluginFile = PluginUtil.getFile(plugin);

        this.daemon.getPluginLoader().unloadPlugin(plugin);  // TODO: Replace with uninstall.

        if (!this.safeDelete(oldPluginFile))
            Runner.runLater(() -> this.safeDelete(oldPluginFile), 10L);
    }
}