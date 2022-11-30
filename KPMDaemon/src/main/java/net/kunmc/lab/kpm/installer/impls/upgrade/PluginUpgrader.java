package net.kunmc.lab.kpm.installer.impls.upgrade;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.installer.AbstractInstaller;
import net.kunmc.lab.kpm.installer.InstallResult;
import net.kunmc.lab.kpm.installer.impls.install.InstallArgument;
import net.kunmc.lab.kpm.installer.impls.install.InstallTasks;
import net.kunmc.lab.kpm.installer.impls.install.PluginInstaller;
import net.kunmc.lab.kpm.installer.impls.uninstall.PluginUninstaller;
import net.kunmc.lab.kpm.installer.impls.uninstall.UnInstallTasks;
import net.kunmc.lab.kpm.installer.impls.uninstall.UninstallArgument;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.InstallFailedSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.PluginNotFoundSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.ResolveFailedSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.UpgradeReadySignal;
import net.kunmc.lab.kpm.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.kpm.installer.task.TaskFailedException;
import net.kunmc.lab.kpm.installer.task.tasks.lookup.LookupArgument;
import net.kunmc.lab.kpm.installer.task.tasks.lookup.LookupResult;
import net.kunmc.lab.kpm.installer.task.tasks.lookup.PluginLookupTask;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.PluginResolveArgument;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.PluginResolveResult;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.PluginResolveTask;
import net.kunmc.lab.kpm.meta.PluginMeta;
import net.kunmc.lab.kpm.meta.PluginMetaProvider;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.utils.KPMCollectors;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * プラグインをアップグレードするインストーラーの実装です。
 * アップグレードは主に以下の流れで動作します。
 *
 * <ol>
 *     <li>{@link UpgradeTasks#SEARCHING_PLUGIN} - アップグレード対象のプラグインを検索する。</li>
 *     <li>{@link UpgradeTasks#CHECKING_ENVIRONMENT} - アップグレードの環境を確認する。</li>
 *     <li>{@link UpgradeTasks#RETRIEVING_METADATA} - 検索したプラグインのメタデータを取得する。</li>
 *     <li>{@link UpgradeTasks#RETRIEVING_UPDATE_QUERY} - プラグインのアップデートクエリを取得する。</li>
 *     <li>{@link UpgradeTasks#RESOLVING_PLUGIN} - アップデートクエリを解決する。</li>
 *     <li>{@link UpgradeTasks#UNINSTALLING_PLUGIN} - プラグインをアンインストールする。</li>
 * </ol>
 * <p>
 * また、このインストーラは{@link net.kunmc.lab.kpm.installer.impls.uninstall.PluginUninstaller} と {@link net.kunmc.lab.kpm.installer.impls.install.PluginInstaller} を内部で呼び出します。
 * そのため、対応したシグナルハンドラを登録しておく必要があります。
 */
public class PluginUpgrader extends AbstractInstaller<UpgradeArgument, UpgradeErrorCause, UpgradeTasks>
{
    public PluginUpgrader(@NotNull KPMDaemon daemon, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(daemon, signalHandler);
    }

    @Override
    public InstallResult<UpgradeTasks> execute(@NotNull UpgradeArgument argument) throws TaskFailedException
    {
        List<Plugin> targetPlugins;
        // region Search plugins
        this.progress.setCurrentTask(UpgradeTasks.SEARCHING_PLUGIN);

        targetPlugins = this.searchPlugin(argument.getTargetPlugins());
        if (targetPlugins == null)
            return this.error(UpgradeErrorCause.PLUGIN_NOT_FOUND);
        // endregion

        // region Do environment check
        this.progress.setCurrentTask(UpgradeTasks.CHECKING_ENVIRONMENT);
        for (Plugin plugin : targetPlugins)
        {
            UpgradeErrorCause mayEnvErrorCause = this.checkEnvironment(plugin.getDescription());
            if (mayEnvErrorCause != null)
                return this.error(mayEnvErrorCause);
        }
        // endregion

        Map<Plugin, String> updateQueries;
        // region Retrieve update queries
        this.progress.setCurrentTask(UpgradeTasks.RETRIEVING_METADATA);
        HashMap<Plugin, PluginMeta> pluginMetas = this.retrievePluginMetadata(targetPlugins);

        this.progress.setCurrentTask(UpgradeTasks.RETRIEVING_UPDATE_QUERY);
        updateQueries = pluginMetas.entrySet().stream().parallel()
                .map(entry ->
                        Pair.of(
                                entry.getKey(),
                                entry.getValue().getResolveQuery() == null ? entry.getKey().getName():
                                        entry.getValue().getResolveQuery()
                        )
                )
                .collect(KPMCollectors.toPairHashMap());
        // endregion

        Map<Plugin, SuccessResult> resolveResults;
        // region Fetch updates
        resolveResults = this.resolvePlugins(updateQueries);
        if (resolveResults == null)
            return this.error(UpgradeErrorCause.PLUGIN_RESOLVE_FAILED);

        // endregion

        // Notify upgrade is ready
        resolveResults = this.notifyUpgradeReady(resolveResults);
        if (resolveResults.isEmpty())  // Cancelled
            return this.error(UpgradeErrorCause.CANCELLED);

        // region Uninstall plugins
        this.progress.setCurrentTask(UpgradeTasks.UNINSTALLING_PLUGIN);

        PluginUninstaller uninstaller;
        try
        {
            uninstaller = new PluginUninstaller(this.daemon, this.signalHandler);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return this.error(UpgradeErrorCause.UNINSTALLER_INSTANTIATION_FAILED);
        }

        InstallResult<UnInstallTasks> uninstallResult = uninstaller.run(
                UninstallArgument.builder(targetPlugins.toArray(new Plugin[0]))
                        .skipExcludeChecks(true)
                        .forceUninstall(true)
                        .build());

        Arrays.stream(uninstallResult.getRemoved()).parallel()
                .forEach(this.progress::addPending);

        if (!uninstallResult.isSuccess())
            return this.error(UpgradeErrorCause.UNINSTALL_FAILED);
        // endregion

        // region Install plugins
        this.progress.setCurrentTask(UpgradeTasks.INSTALLING_PLUGIN);
        for (SuccessResult resolveResult : resolveResults.values())
        {
            PluginInstaller installer;
            try
            {
                installer = new PluginInstaller(this.daemon, this.signalHandler);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return this.error(UpgradeErrorCause.INSTALLER_INSTANTIATION_FAILED);
            }

            InstallResult<InstallTasks> installResult = installer.run(
                    InstallArgument.builder(resolveResult)
                            .forceInstall(true)
                            .build());

            if (installResult.isSuccess())
                continue;

            // Install failed
            InstallFailedSignal signal = new InstallFailedSignal(installResult);
            this.postSignal(signal);
            if (!signal.isContinueUpgrade())
                return this.error(UpgradeErrorCause.INSTALL_FAILED);
        }

        // endregion

        // region clean VM(Unlink old Plugin data)
        targetPlugins.clear();
        resolveResults.clear();
        updateQueries.clear();
        pluginMetas.clear();

        System.gc();
        // endregion

        return this.success();
    }

    @Nullable
    private UpgradeErrorCause checkEnvironment(PluginDescriptionFile pluginDescription)
    {
        String pluginName = pluginDescription.getName();

        // region Check if the plugin is marked as excluded
        if (this.isPluginIgnored(pluginName))
        {
            IgnoredPluginSignal ignoredPluginSignal = new IgnoredPluginSignal(pluginDescription);
            this.postSignal(ignoredPluginSignal);

            if (ignoredPluginSignal.isContinueInstall())
                return UpgradeErrorCause.PLUGIN_EXCLUDED;
        }
        // endregion

        return null;  // No error with environment.
    }

    private Map<Plugin, SuccessResult> notifyUpgradeReady(@NotNull Map<Plugin, SuccessResult> resolveResults)
    {
        UpgradeReadySignal resolvedSignal = new UpgradeReadySignal(resolveResults);
        this.postSignal(resolvedSignal);

        return resolvedSignal.getPlugins().stream().parallel()
                .filter(UpgradeReadySignal.ResolvedPluginElement::isContinueUpgrade)
                .map(element -> Pair.of(element.getPlugin(), element.getResolveResult()))
                .collect(KPMCollectors.toPairHashMap());
    }

    private HashMap<Plugin, PluginMeta> retrievePluginMetadata(@NotNull List<Plugin> targets)
    {
        PluginMetaProvider metaProvider = this.daemon.getPluginMetaManager().getProvider();
        return targets.stream()
                .map(plugin -> Pair.of(plugin, metaProvider.getPluginMeta(plugin.getName())))
                .collect(KPMCollectors.toPairHashMap());
    }

    private List<Plugin> searchPlugin(@Nullable List<String> targets) throws TaskFailedException
    {
        if (targets == null)
            return new ArrayList<>(Arrays.asList(Bukkit.getPluginManager().getPlugins()));

        LookupResult lookupResult = this.submitter(UpgradeTasks.SEARCHING_PLUGIN, new PluginLookupTask(this))
                .submitAll(new LookupArgument(targets.toArray(new String[0])));

        HashMap<String, Plugin> foundPlugins = lookupResult.getPlugins();
        assert foundPlugins != null;

        targets.removeAll(foundPlugins.keySet());

        if (!targets.isEmpty())
        {
            boolean isContinue = targets.stream().allMatch(target -> {
                PluginNotFoundSignal signal = new PluginNotFoundSignal(target);
                PluginUpgrader.this.postSignal(signal);
                return signal.isContinueUpgrade();
            });

            if (!isContinue)
                return null;
        }

        return new ArrayList<>(foundPlugins.values());
    }

    private Map<Plugin, SuccessResult> resolvePlugins(Map<Plugin, String> queries)
    {
        Map<Plugin, SuccessResult> resolvedQueries = new HashMap<>();

        try
        {
            for (Map.Entry<Plugin, String> entry : queries.entrySet())
            {
                Plugin plugin = entry.getKey();
                String query = entry.getValue();

                ResolveResult result = this.resolveOne(plugin, query);

                if (result == null)
                    return null; // Cancelled

                resolvedQueries.put(plugin, (SuccessResult) result);
            }
        }
        catch (TaskFailedException ignored)
        {
            resolvedQueries = null;
        }

        return resolvedQueries;
    }

    private ResolveResult resolveOne(Plugin plugin, String query) throws TaskFailedException
    {
        try
        {
            PluginResolveResult result = this.submitter(UpgradeTasks.RESOLVING_PLUGIN, new PluginResolveTask(this))
                    .submitAll(new PluginResolveArgument(query));

            return result.getResolveResult();
        }
        catch (TaskFailedException e)
        {
            assert e.getResult() instanceof PluginResolveResult;
            PluginResolveResult result = (PluginResolveResult) e.getResult();

            assert result.getErrorCause() != null;
            ResolveFailedSignal signal = new ResolveFailedSignal(plugin, result.getErrorCause(), result.getState());
            this.postSignal(signal);

            if (signal.isContinueUpgrade())
                return null;
            else
                throw e;
        }
    }
}