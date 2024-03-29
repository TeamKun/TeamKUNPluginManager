package org.kunlab.kpm.installer.impls.upgrade;

import net.kunmc.lab.peyangpaperutils.collectors.ExCollectors;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.installer.AbstractInstaller;
import org.kunlab.kpm.installer.impls.install.InstallArgument;
import org.kunlab.kpm.installer.impls.install.InstallTasks;
import org.kunlab.kpm.installer.impls.install.PluginInstaller;
import org.kunlab.kpm.installer.impls.uninstall.PluginUninstallSucceedResult;
import org.kunlab.kpm.installer.impls.uninstall.PluginUninstaller;
import org.kunlab.kpm.installer.impls.uninstall.UnInstallTasks;
import org.kunlab.kpm.installer.impls.uninstall.UninstallArgument;
import org.kunlab.kpm.installer.impls.upgrade.signals.InstallFailedSignal;
import org.kunlab.kpm.installer.impls.upgrade.signals.InvalidPluginVersionSignal;
import org.kunlab.kpm.installer.impls.upgrade.signals.PluginNotFoundSignal;
import org.kunlab.kpm.installer.impls.upgrade.signals.ResolveFailedSignal;
import org.kunlab.kpm.installer.impls.upgrade.signals.UpgradeReadySignal;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.installer.signals.assertion.IgnoredPluginSignal;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.kpminfo.KPMInformationFile;
import org.kunlab.kpm.meta.interfaces.PluginMeta;
import org.kunlab.kpm.meta.interfaces.PluginMetaProvider;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;
import org.kunlab.kpm.resolver.interfaces.result.SuccessResult;
import org.kunlab.kpm.signal.SignalHandleManager;
import org.kunlab.kpm.task.TaskFailedException;
import org.kunlab.kpm.task.interfaces.dependencies.DependencyElement;
import org.kunlab.kpm.task.tasks.dependencies.DependencyElementImpl;
import org.kunlab.kpm.task.tasks.dependencies.computer.DependsComputeOrderArgument;
import org.kunlab.kpm.task.tasks.dependencies.computer.DependsComputeOrderTask;
import org.kunlab.kpm.task.tasks.install.PluginsInstallArgument;
import org.kunlab.kpm.task.tasks.install.PluginsInstallTask;
import org.kunlab.kpm.task.tasks.lookup.LookupArgument;
import org.kunlab.kpm.task.tasks.lookup.LookupResult;
import org.kunlab.kpm.task.tasks.lookup.PluginLookupTask;
import org.kunlab.kpm.task.tasks.resolve.PluginResolveArgument;
import org.kunlab.kpm.task.tasks.resolve.PluginResolveResult;
import org.kunlab.kpm.task.tasks.resolve.PluginResolveTask;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginIsDependencySignal;
import org.kunlab.kpm.versioning.Version;

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
 *     <li>{@link UpgradeTasks#COMPUTING_DEPENDENCY_LOAD_ORDER} - 依存関係の読み込み順を計算する。</li>
 *     <li>{@link UpgradeTasks#RE_LOADING_DEPENDENCIES} - 依存関係のプラグインを再読み込みする。</li>
 * </ol>
 * <p>
 * また、このインストーラは{@link PluginUninstaller} と {@link PluginInstaller} を内部で呼び出します。
 * そのため、対応したシグナルハンドラを登録しておく必要があります。
 */
public class PluginUpgrader extends AbstractInstaller<UpgradeArgument, UpgradeErrorCause, UpgradeTasks>
{
    public PluginUpgrader(@NotNull KPMRegistry registry, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(registry, signalHandler);
        this.progress.setCurrentTask(UpgradeTasks.INITIALIZED);
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

        if (targetPlugins.removeIf(plugin -> plugin.getName().equalsIgnoreCase("TeamKUNPluginManager")) &&
                targetPlugins.isEmpty())
            return this.error(UpgradeErrorCause.SELF_UPGRADE_ATTEMPTED);

        // endregion

        Map<Plugin, String> updateQueries;
        // region Retrieve update queries
        this.progress.setCurrentTask(UpgradeTasks.RETRIEVING_METADATA);
        Map<Plugin, PluginMeta> pluginMetas = this.retrievePluginMetadata(targetPlugins);
        this.progress.setCurrentTask(UpgradeTasks.RETRIEVING_UPDATE_QUERY);

        updateQueries = this.retrieveUpdateQuery(pluginMetas);
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
        // endregion

        resolveResults = this.notifyUpgradeReady(resolveResults);
        if (resolveResults.isEmpty())  // Cancelled
            return this.error(UpgradeErrorCause.CANCELLED);
        targetPlugins = new ArrayList<>(resolveResults.keySet());

        return this.modifyPlugins(targetPlugins, resolveResults);
    }

    private InstallResult<UpgradeTasks> modifyPlugins(List<Plugin> targetPlugins, Map<? extends Plugin, ? extends SuccessResult> resolveResults)
    {
        Map<PluginDescriptionFile, Path> unloadedPlugins;
        // region Uninstall plugins
        this.progress.setCurrentTask(UpgradeTasks.UNINSTALLING_PLUGIN);
        try
        {
            unloadedPlugins = this.uninstallPlugins(targetPlugins);

            if (unloadedPlugins == null)  // So, uninstall is failed
                return this.error(UpgradeErrorCause.UNINSTALL_FAILED);
        }
        catch (IOException e)
        {
            this.registry.getExceptionHandler().report(e);
            return this.error(UpgradeErrorCause.UNINSTALLER_INSTANTIATION_FAILED);
        }
        // endregion

        Map<PluginDescriptionFile, SuccessResult> resolveResultMap = resolveResults.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getDescription(), Map.Entry::getValue));

        // region Install plugins
        this.progress.setCurrentTask(UpgradeTasks.INSTALLING_PLUGIN);

        for (Map.Entry<PluginDescriptionFile, SuccessResult> entry : resolveResultMap.entrySet())
        {
            unloadedPlugins.remove(entry.getKey());  // unloadedPlugins will be used to restore unloaded dependencies, so we need to ignore plugins that will be installed.
            SuccessResult resolveResult = entry.getValue();

            PluginInstaller installer;
            try
            {
                installer = new PluginInstaller(this.registry, this.signalHandler);
            }
            catch (IOException e)
            {
                this.registry.getExceptionHandler().report(e);
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
        UpgradeErrorCause mayError = this.restoreUnloadedPlugin(unloadedPlugins);

        if (mayError != null)
            return this.error(mayError);
        // endregion

        return this.success();
    }

    private Map<PluginDescriptionFile, Path> uninstallPlugins(List<Plugin> targetPlugins) throws IOException
    {
        PluginUninstaller uninstaller = new PluginUninstaller(this.registry, this.signalHandler);

        InstallResult<UnInstallTasks> uninstallResult = uninstaller.run(
                UninstallArgument.builder(targetPlugins.toArray(new Plugin[0]))
                        .skipExcludeChecks(true)
                        .forceUninstall(true)
                        .onDependencyFound(PluginIsDependencySignal.Operation.DISABLE)
                        .build());

        Arrays.stream(uninstallResult.getRemoved()).parallel()
                .forEach(this.progress::addPending);

        if (!uninstallResult.isSuccess())
            return null;

        PluginUninstallSucceedResult uninstallSucceedResult = (PluginUninstallSucceedResult) uninstallResult;

        return uninstallSucceedResult.getResult().getUnloadedPlugins();
    }

    private Map<Plugin, String> retrieveUpdateQuery(Map<Plugin, PluginMeta> pluginMetas)
    {
        Map<Plugin, String> result = new HashMap<>();

        for (Map.Entry<Plugin, PluginMeta> entry : pluginMetas.entrySet())
        {
            Plugin plugin = entry.getKey();
            PluginMeta meta = entry.getValue();
            KPMInformationFile kpmInfo = this.registry.getKpmInfoManager().hasInfo(plugin) ?
                    this.registry.getKpmInfoManager().getInfo(plugin): null;

            String query;
            if (!(kpmInfo == null || kpmInfo.getUpdateQuery() == null))  // KPM info's update query is the highest priority.
                query = kpmInfo.getUpdateQuery().toString();
            else if (meta.getResolveQuery() != null)
                query = meta.getResolveQuery();
            else
                query = meta.getName();

            result.put(plugin, query);
        }

        return result;
    }

    private UpgradeErrorCause restoreUnloadedPlugin(Map<PluginDescriptionFile, ? extends Path> unloadedPlugins)
    {
        List<DependencyElement> dependencyElements = unloadedPlugins.entrySet().stream()
                .map(entry -> {
                    PluginDescriptionFile description = entry.getKey();
                    Path pluginPath = entry.getValue();
                    return new DependencyElementImpl(
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

    private UpgradeErrorCause excludeOrCancel(Plugin plugin, UpgradeErrorCause cause, Version currentVersion, Version newVersion)
    {
        InvalidPluginVersionSignal signal = new InvalidPluginVersionSignal(plugin, cause, currentVersion, newVersion);
        this.postSignal(signal);
        if (!signal.isContinueUpgrade())
            return cause;
        else if (signal.isExcludePlugin())
            return UpgradeErrorCause.PLUGIN_EXCLUDED;
        else
            return null;
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
                .collect(ExCollectors.toPairHashMap());
    }

    private Map<Plugin, PluginMeta> retrievePluginMetadata(@NotNull List<Plugin> targets)
    {
        PluginMetaProvider metaProvider = this.registry.getPluginMetaManager().getProvider();
        return targets.stream()
                .filter(metaProvider::isPluginMetaExists)
                .map(plugin -> Pair.of(plugin, metaProvider.getPluginMeta(plugin.getName())))
                .collect(ExCollectors.toPairHashMap());
    }

    private List<Plugin> searchPlugin(@Nullable List<String> targets) throws TaskFailedException
    {
        if (targets == null)
            return new ArrayList<>(Arrays.asList(Bukkit.getPluginManager().getPlugins()));

        LookupResult lookupResult = this.submitter(UpgradeTasks.SEARCHING_PLUGIN, new PluginLookupTask(this))
                .submitAll(new LookupArgument(targets.toArray(new String[0])));

        assert lookupResult.getPlugins() != null;
        Map<String, Plugin> foundPlugins = lookupResult.getPlugins().entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(ExCollectors.toHashMap());

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
