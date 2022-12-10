package net.kunmc.lab.kpm.installer.impls.upgrade;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.installer.AbstractInstaller;
import net.kunmc.lab.kpm.installer.InstallResult;
import net.kunmc.lab.kpm.installer.impls.install.InstallArgument;
import net.kunmc.lab.kpm.installer.impls.install.InstallTasks;
import net.kunmc.lab.kpm.installer.impls.install.PluginInstaller;
import net.kunmc.lab.kpm.installer.impls.uninstall.PluginUninstallSucceedResult;
import net.kunmc.lab.kpm.installer.impls.uninstall.PluginUninstaller;
import net.kunmc.lab.kpm.installer.impls.uninstall.UnInstallTasks;
import net.kunmc.lab.kpm.installer.impls.uninstall.UninstallArgument;
import net.kunmc.lab.kpm.installer.impls.uninstall.signals.PluginIsDependencySignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.InstallFailedSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.InvalidPluginVersionSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.PluginNotFoundSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.ResolveFailedSignal;
import net.kunmc.lab.kpm.installer.impls.upgrade.signals.UpgradeReadySignal;
import net.kunmc.lab.kpm.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.kpm.installer.task.TaskFailedException;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.computer.DependsComputeOrderArgument;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.computer.DependsComputeOrderTask;
import net.kunmc.lab.kpm.installer.task.tasks.install.PluginsInstallArgument;
import net.kunmc.lab.kpm.installer.task.tasks.install.PluginsInstallTask;
import net.kunmc.lab.kpm.installer.task.tasks.lookup.LookupArgument;
import net.kunmc.lab.kpm.installer.task.tasks.lookup.LookupResult;
import net.kunmc.lab.kpm.installer.task.tasks.lookup.PluginLookupTask;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.PluginResolveArgument;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.PluginResolveResult;
import net.kunmc.lab.kpm.installer.task.tasks.resolve.PluginResolveTask;
import net.kunmc.lab.kpm.kpminfo.KPMInformationFile;
import net.kunmc.lab.kpm.meta.PluginMeta;
import net.kunmc.lab.kpm.meta.PluginMetaProvider;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.utils.KPMCollectors;
import net.kunmc.lab.kpm.utils.versioning.Version;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 *     <li>{@link UpgradeTasks#INSTALLING_PLUGIN} - プラグインをインストールする。</li>
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

        Map<Plugin, String> updateQueries;
        // region Retrieve update queries
        this.progress.setCurrentTask(UpgradeTasks.RETRIEVING_METADATA);
        HashMap<Plugin, PluginMeta> pluginMetas = this.retrievePluginMetadata(targetPlugins);
        this.progress.setCurrentTask(UpgradeTasks.RETRIEVING_UPDATE_QUERY);

        updateQueries = new HashMap<>();
        for (Map.Entry<Plugin, PluginMeta> entry : pluginMetas.entrySet())
        {
            Plugin plugin = entry.getKey();
            PluginMeta meta = entry.getValue();
            KPMInformationFile kpmInfo = this.daemon.getKpmInfoManager().hasInfo(plugin) ?
                    this.daemon.getKpmInfoManager().getInfo(plugin): null;

            String query;
            if (!(kpmInfo == null || kpmInfo.getUpdateQuery() == null))  // KPM info's update query is the highest priority.
                query = kpmInfo.getUpdateQuery().toString();
            else if (meta.getResolveQuery() != null)
                query = meta.getResolveQuery();
            else
                query = meta.getName();

            updateQueries.put(plugin, query);
        }
        // endregion

        Map<Plugin, SuccessResult> resolveResults;
        // region Fetch updates
        resolveResults = this.resolvePlugins(updateQueries);
        if (resolveResults == null)
            return this.error(UpgradeErrorCause.PLUGIN_RESOLVE_FAILED);

        // endregion

        // region Check plugin condition(such as version not defined or version is not higher than current version etc.)
        for (Map.Entry<Plugin, SuccessResult> entry : new ArrayList<>(resolveResults.entrySet()))
        {
            UpgradeErrorCause mayErrorCause = this.checkPluginMatch(entry.getKey(), entry.getValue());
            if (mayErrorCause == UpgradeErrorCause.PLUGIN_EXCLUDED)
                resolveResults.remove(entry.getKey());
            else if (mayErrorCause != null)
                return this.error(mayErrorCause);
        }

        // Notify upgrade is ready or nothing to upgrade
        if (resolveResults.isEmpty())
            return this.error(UpgradeErrorCause.UP_TO_DATE);

        // region Do environment check
        this.progress.setCurrentTask(UpgradeTasks.CHECKING_ENVIRONMENT);
        for (Plugin plugin : resolveResults.keySet())
        {
            UpgradeErrorCause mayEnvErrorCause = this.checkEnvironment(plugin.getDescription());
            if (mayEnvErrorCause != null)
                return this.error(mayEnvErrorCause);
        }
        // endregion

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
                        .onDependencyFound(PluginIsDependencySignal.Operation.DISABLE)
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

        // Restore unloaded dependencies
        PluginUninstallSucceedResult uninstallSucceedResult = (PluginUninstallSucceedResult) uninstallResult;
        UpgradeErrorCause mayError = this.restoreUnloadedPlugin(uninstallSucceedResult.getResult().getUnloadedPlugins());

        if (mayError != null)
            return this.error(mayError);
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

    private UpgradeErrorCause restoreUnloadedPlugin(Map<PluginDescriptionFile, Path> unloadedPlugins)
    {
        List<DependencyElement> dependencyElements = unloadedPlugins.entrySet().stream()
                .map(entry -> {
                    PluginDescriptionFile description = entry.getKey();
                    Path pluginPath = entry.getValue();
                    return new DependencyElement(
                            description.getName(),
                            pluginPath,
                            description,
                            null,
                            null
                    );
                })
                .collect(Collectors.toList());

        try
        {
            this.submitter(
                            UpgradeTasks.COMPUTING_DEPENDENCY_LOAD_ORDER,
                            new DependsComputeOrderTask(this)
                    )
                    .then(
                            UpgradeTasks.INSTALLING_PLUGIN,
                            new PluginsInstallTask(this)
                    )
                    .bridgeArgument(result -> new PluginsInstallArgument(result.getOrder()))
                    .submitAll(new DependsComputeOrderArgument(dependencyElements));
        }
        catch (TaskFailedException ignored)
        {
            return UpgradeErrorCause.INSTALL_FAILED;
        }

        return null;
    }

    private UpgradeErrorCause checkPluginMatch(Plugin plugin, SuccessResult resolveResult)
    {
        PluginDescriptionFile description = plugin.getDescription();

        Version currentVersion = Version.ofNullable(description.getVersion());
        if (currentVersion == null)  // The parser failed to parse version.
        {
            UpgradeErrorCause excludeCause = this.excludeOrCancel(plugin,
                    UpgradeErrorCause.PLUGIN_VERSION_FORMAT_MALFORMED, null, null
            );

            if (excludeCause != UpgradeErrorCause.PLUGIN_EXCLUDED)
                return excludeCause;

            // Cause is PLUGIN_VERSION_FORMAT_MALFORMED, but we can still upgrade
        }


        if (resolveResult.getVersion() == null)
            return this.excludeOrCancel(plugin, UpgradeErrorCause.PLUGIN_VERSION_NOT_DEFINED, currentVersion, null);

        Version newVersion = Version.ofNullable(resolveResult.getVersion());

        if (newVersion == null)  // The parser failed to parse version.
            return this.excludeOrCancel(plugin, UpgradeErrorCause.PLUGIN_VERSION_FORMAT_MALFORMED, currentVersion, null);

        if (currentVersion == null)
            return null;  // Response with no error because currentVersion is not defined(or malformed), so we don't have to check version is newer or equal.

        if (currentVersion.isNewerThanOrEqualTo(newVersion))
            return this.excludeOrCancel(plugin, UpgradeErrorCause.PLUGIN_IS_OLDER_OR_EQUAL, currentVersion, newVersion);

        return null;
    }

    /**
     * Ask user to exclude the plugin or cancel the upgrade.
     *
     * @param plugin         The plugin to exclude.
     * @param cause          The cause of the exclusion.
     * @param currentVersion The current version of the plugin.
     * @param newVersion     The new version of the plugin.
     * @return {@link UpgradeErrorCause#PLUGIN_EXCLUDED} if the plugin is excluded, or the cause if the upgrade is cancelled.
     */
    private UpgradeErrorCause excludeOrCancel(Plugin plugin, UpgradeErrorCause cause, Version currentVersion, Version newVersion)
    {
        InvalidPluginVersionSignal signal = new InvalidPluginVersionSignal(plugin, cause, currentVersion, newVersion);
        this.postSignal(signal);
        return signal.isContinueUpgrade() ? UpgradeErrorCause.PLUGIN_EXCLUDED: cause;
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

                if (result == null)  // User(or signal) selected to skip this plugin
                    continue; // Skip

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
