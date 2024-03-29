package org.kunlab.kpm.installer.impls.uninstall;

import net.kunmc.lab.peyangpaperutils.collectors.ExCollectors;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.AbstractInstaller;
import org.kunlab.kpm.installer.impls.uninstall.signals.SearchingPluginSignal;
import org.kunlab.kpm.installer.impls.uninstall.signals.UninstallReadySignal;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.installer.signals.assertion.IgnoredPluginSignal;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.meta.interfaces.DependencyNode;
import org.kunlab.kpm.signal.SignalHandleManager;
import org.kunlab.kpm.task.TaskFailedException;
import org.kunlab.kpm.task.interfaces.dependencies.DependencyElement;
import org.kunlab.kpm.task.tasks.dependencies.DependencyElementImpl;
import org.kunlab.kpm.task.tasks.dependencies.computer.DependsComputeOrderArgument;
import org.kunlab.kpm.task.tasks.dependencies.computer.DependsComputeOrderTask;
import org.kunlab.kpm.task.tasks.uninstall.UnInstallResult;
import org.kunlab.kpm.task.tasks.uninstall.UnInstallTask;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginIsDependencySignal;
import org.kunlab.kpm.utils.PluginUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * プラグインをアンインストールするインストーラーの実装です。
 * インストーラは主に以下の流れで動作します。
 *
 * <ol>
 *     <li>{@link UnInstallTasks#SEARCHING_PLUGIN} - アンインストール対象のプラグインを検索する。</li>
 *     <li>{@link UnInstallTasks#CHECKING_ENVIRONMENT} - 環境が適合しているかどうかをチェックする。</li>
 *     <li>{@link UnInstallTasks#COMPUTING_UNINSTALL_ORDER} - アンインストールする順序を、依存関係を考慮して計算する。</li>
 *     <li>{@link UnInstallTasks#UNINSTALLING_PLUGINS} - プラグインをアンインストールする。</li>
 * </ol>
 */
public class PluginUninstaller extends AbstractInstaller<UninstallArgument, UnInstallErrorCause, UnInstallTasks>
{
    public PluginUninstaller(@NotNull KPMRegistry registry, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(registry, signalHandler);
        this.progress.setCurrentTask(UnInstallTasks.INITIALIZED);
    }

    @Override
    public InstallResult<UnInstallTasks> execute(@NotNull UninstallArgument argument) throws TaskFailedException
    {
        List<Plugin> uninstallTargets = new ArrayList<>();
        // region Search plugin
        this.progress.setCurrentTask(UnInstallTasks.SEARCHING_PLUGIN);

        if (argument.getPluginNames() != null)
            for (String pluginName : argument.getPluginNames())
            {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
                if (plugin == null)
                    return this.error(UnInstallErrorCause.PLUGIN_NOT_FOUND);

                uninstallTargets.add(plugin);
            }
        else
        {
            assert argument.getPlugins() != null;
            uninstallTargets.addAll(argument.getPlugins());
        }

        // endregion

        PluginIsDependencySignal.Operation dependencyBehavior = null;
        List<String> uninstallDependencies = new ArrayList<>();
        // region Check other plugins depends on this plugin.
        if (!argument.isSkipDependencyChecks())
        {
            List<Plugin> additionalTargets = new ArrayList<>();

            for (Plugin plugin : uninstallTargets)
            {
                List<DependencyNode> dependencies = this.getDependenciesRecursive(plugin);

                // Remove plugin which is marked as dependency because it is marked to be uninstalled.
                dependencies.removeIf(
                        dependencyNode -> dependencyNode.getPlugin().equalsIgnoreCase(plugin.getName())
                );

                if (dependencies.isEmpty())
                    continue;

                PluginIsDependencySignal depSignal =
                        new PluginIsDependencySignal(plugin, dependencies);

                this.postSignal(depSignal);

                PluginIsDependencySignal.Operation operation =
                        depSignal.getOperation();
                if (operation == null)
                    operation = argument.getOnDependencyFound();

                if (operation == PluginIsDependencySignal.Operation.CANCEL)
                    return this.error(UnInstallErrorCause.PLUGIN_IS_DEPENDENCY);  // Cancel uninstallation.
                else if (operation == PluginIsDependencySignal.Operation.IGNORE)
                    continue; // Ignore these dependencies

                dependencyBehavior = operation;
                additionalTargets.addAll(dependencies.stream().parallel()
                        .map(dependencyNode -> Bukkit.getPluginManager().getPlugin(dependencyNode.getPlugin()))
                        .collect(Collectors.toList()));
                uninstallDependencies.addAll(dependencies.stream().parallel()
                        .map(DependencyNode::getPlugin)
                        .collect(Collectors.toList()));
            }

            uninstallTargets.addAll(additionalTargets);
        }

        if (dependencyBehavior == null)
            dependencyBehavior = argument.getOnDependencyFound();  // Typically, this is set in above if statement.
        // endregion

        // region Do assertions.
        this.progress.setCurrentTask(UnInstallTasks.CHECKING_ENVIRONMENT);

        // region Check is plugin marked as ignored
        if (!argument.isSkipExcludeChecks())
        {
            for (Plugin plugin : uninstallTargets)
            {
                if (!this.isPluginIgnored(plugin.getName()))
                    continue;
                IgnoredPluginSignal ignoredPluginSignal = new IgnoredPluginSignal(plugin.getDescription());
                this.postSignal(ignoredPluginSignal);
                if (!(argument.isForceUninstall() || ignoredPluginSignal.isContinueInstall()))
                    return this.error(UnInstallErrorCause.PLUGIN_IGNORED);
            }
        }
        // endregion


        // endregion

        UninstallReadySignal uninstallReadySignal =
                new UninstallReadySignal(uninstallTargets, argument.isAutoConfirm());
        this.postSignal(uninstallReadySignal);
        if (!uninstallReadySignal.isContinueUninstall())
            return this.error(UnInstallErrorCause.CANCELLED);

        uninstallTargets = uninstallReadySignal.getPlugins();

        // region Uninstall plugin

        // Before uninstall plugin, we need to compute the order of plugins to uninstall.
        // Reuse DepenedsComputeOrderTask to compute the order, so we need to map List<Plugin> to List<DependencyElement>.
        List<DependencyElement> computeOrderTarget = uninstallTargets.stream().parallel()
                .map(pl -> {
                    PluginDescriptionFile descriptionFile = pl.getDescription();
                    String name = descriptionFile.getName();
                    Path path = PluginUtil.getFile(pl).toPath();

                    // Dummy element
                    return new DependencyElementImpl(name, path, descriptionFile, null, null);
                })
                .collect(Collectors.toList());

        // Create String-Plugin map to DependencyElement doesn't have Plugin instance.
        Map<String, Plugin> namePluginMap = uninstallTargets.stream().parallel()
                .collect(Collectors.toMap(Plugin::getName, pl -> pl));

        PluginIsDependencySignal.Operation finalDependencyBehavior = dependencyBehavior;  // for lambda
        UnInstallResult uninstallResult = this.submitter(
                        UnInstallTasks.COMPUTING_UNINSTALL_ORDER,
                        new DependsComputeOrderTask(this)
                )
                .then(
                        UnInstallTasks.UNINSTALLING_PLUGINS,
                        new UnInstallTask(this)
                )
                .bridgeArgument(computeResult -> {
                    List<DependencyElement> ordered = computeResult.getOrder();
                    List<Plugin> orderedPlugins = ordered.stream()
                            .map(element -> namePluginMap.get(element.getPluginName()))
                            .collect(ExCollectors.toReversedList()); // Convert load order to unload order.

                    return new org.kunlab.kpm.task.tasks.uninstall.UninstallArgument(orderedPlugins, uninstallDependencies, finalDependencyBehavior);
                })
                .submitAll(new DependsComputeOrderArgument(computeOrderTarget));
        // endregion

        return this.success(new PluginUninstallSucceedResult(this.progress, uninstallResult));
    }

    private Plugin getPlugin(String query)
    {
        SearchingPluginSignal searchingPluginSignal = new SearchingPluginSignal(query);
        this.postSignal(searchingPluginSignal);
        String modifiedQuery = searchingPluginSignal.getQuery(); // May be changed by signal handler

        Plugin plugin = Bukkit.getPluginManager().getPlugin(modifiedQuery);

        if (!PluginUtil.isPluginLoaded(plugin))
            return null;

        return plugin;
    }

    private List<DependencyNode> getDependenciesRecursive(Plugin target)
    {
        List<DependencyNode> dependencyPlugins = new ArrayList<>();

        // Retrieve plugins that depends on target plugin.
        List<DependencyNode> dependencies =
                this.registry.getPluginMetaManager().getProvider().getDependedBy(target.getName());
        dependencies.addAll(this.registry.getPluginMetaManager().getProvider().getSoftDependedBy(target.getName()));

        // Loop through all dependencies to find dependency's dependency.
        for (DependencyNode depend : dependencies)
        {
            Plugin dependPlugin = this.getPlugin(depend.getPlugin());
            if (dependPlugin != null)
            {
                dependencyPlugins.add(depend);
                dependencyPlugins.addAll(this.getDependenciesRecursive(dependPlugin));
            }
            // if it cannot find the plugin, ignore it because it only exist in database.
            // Doesn't exist in filesystem and server plugins.
        }

        return dependencyPlugins;
    }
}
