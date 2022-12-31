package net.kunmc.lab.kpm.installer.impls.uninstall;

import net.kunmc.lab.kpm.KPMRegistry;
import net.kunmc.lab.kpm.installer.AbstractInstaller;
import net.kunmc.lab.kpm.installer.impls.uninstall.signals.PluginIsDependencySignal;
import net.kunmc.lab.kpm.installer.impls.uninstall.signals.SearchingPluginSignal;
import net.kunmc.lab.kpm.installer.impls.uninstall.signals.UninstallReadySignal;
import net.kunmc.lab.kpm.installer.task.TaskFailedException;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.computer.DependsComputeOrderArgument;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.computer.DependsComputeOrderTask;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UnInstallResult;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UnInstallTask;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.interfaces.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.kpm.meta.DependencyNode;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.utils.KPMCollectors;
import net.kunmc.lab.kpm.utils.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

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
public class PluginUninstaller extends AbstractInstaller<net.kunmc.lab.kpm.installer.impls.uninstall.UninstallArgument, UnInstallErrorCause, UnInstallTasks>
{
    public PluginUninstaller(@NotNull KPMRegistry registry, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(registry, signalHandler);
        this.progress.setCurrentTask(UnInstallTasks.INITIALIZED);
    }

    @Override
    public InstallResult<UnInstallTasks> execute(@NotNull net.kunmc.lab.kpm.installer.impls.uninstall.UninstallArgument argument) throws TaskFailedException
    {
        List<Plugin> installTargets = new ArrayList<>();
        // region Search plugin
        this.progress.setCurrentTask(UnInstallTasks.SEARCHING_PLUGIN);

        if (argument.getPluginNames() != null)
            for (String pluginName : argument.getPluginNames())
            {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
                if (plugin == null)
                    return this.error(UnInstallErrorCause.PLUGIN_NOT_FOUND);

                installTargets.add(plugin);
            }
        else
        {
            assert argument.getPlugins() != null;
            installTargets.addAll(argument.getPlugins());
        }

        // endregion

        // region Do assertions.
        this.progress.setCurrentTask(UnInstallTasks.CHECKING_ENVIRONMENT);

        // region Check is plugin marked as ignored
        if (!argument.isSkipExcludeChecks())
        {
            for (Plugin plugin : installTargets)
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

        PluginIsDependencySignal.Operation dependencyBehavior = null;
        List<String> uninstallDependencies = new ArrayList<>();
        // region Check other plugins depends on this plugin.
        if (!argument.isSkipDependencyChecks())
        {
            for (Plugin plugin : installTargets)
            {
                List<DependencyNode> dependencies = this.getDependenciesRecursive(plugin);

                // Remove plugin which is marked as dependency because it is marked to be uninstalled.
                dependencies.removeIf(
                        dependencyNode -> dependencyNode.getDependsOn().equalsIgnoreCase(plugin.getName())
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

                dependencyBehavior = operation;
                installTargets.addAll(dependencies.stream().parallel()
                        .map(dependencyNode -> Bukkit.getPluginManager().getPlugin(dependencyNode.getDependsOn()))
                        .collect(Collectors.toList()));
                uninstallDependencies.addAll(dependencies.stream().parallel()
                        .map(DependencyNode::getDependsOn)
                        .collect(Collectors.toList()));
            }
        }

        if (dependencyBehavior == null)
            dependencyBehavior = argument.getOnDependencyFound();  // Typically, this is set in above if statement.
        // endregion

        // endregion

        UninstallReadySignal uninstallReadySignal =
                new UninstallReadySignal(installTargets, argument.isAutoConfirm());
        this.postSignal(uninstallReadySignal);
        if (!uninstallReadySignal.isContinueUninstall())
            return this.error(UnInstallErrorCause.CANCELLED);

        installTargets = uninstallReadySignal.getPlugins();

        // region Uninstall plugin

        // Before uninstall plugin, we need to compute the order of plugins to uninstall.
        // Reuse DepenedsComputeOrderTask to compute the order, so we need to map List<Plugin> to List<DependencyElement>.
        List<DependencyElement> computeOrderTarget = installTargets.stream().parallel()
                .map(pl -> {
                    PluginDescriptionFile descriptionFile = pl.getDescription();
                    String name = descriptionFile.getName();
                    Path path = PluginUtil.getFile(pl).toPath();

                    // Dummy element
                    return new DependencyElement(name, path, descriptionFile, null, null);
                })
                .collect(Collectors.toList());

        // Create String-Plugin map to DependencyElement doesn't have Plugin instance.
        Map<String, Plugin> namePluginMap = installTargets.stream().parallel()
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
                            .collect(KPMCollectors.toReversedList()); // Convert load order to unload order.

                    return new net.kunmc.lab.kpm.installer.task.tasks.uninstall.UninstallArgument(orderedPlugins, uninstallDependencies, finalDependencyBehavior);
                })
                .submitAll(new DependsComputeOrderArgument(computeOrderTarget));
        // endregion

        return this.success(new PluginUninstallSucceedResult(this.progress, uninstallResult));
    }

    private Plugin getPlugin(String query)
    {
        SearchingPluginSignal searchingPluginSignal = new SearchingPluginSignal(query);
        this.postSignal(searchingPluginSignal);
        query = searchingPluginSignal.getQuery(); // May be changed by signal handler

        Plugin plugin = Bukkit.getPluginManager().getPlugin(query);

        if (!PluginUtil.isPluginLoaded(plugin))
            return null;

        return plugin;
    }

    private ArrayList<DependencyNode> getDependenciesRecursive(Plugin target)
    {
        ArrayList<DependencyNode> dependencyPlugins = new ArrayList<>();

        // Retrieve plugins that depends on target plugin.
        List<DependencyNode> dependencies =
                this.registry.getPluginMetaManager().getProvider().getDependedBy(target.getName());

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