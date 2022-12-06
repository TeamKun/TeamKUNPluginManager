package net.kunmc.lab.kpm.installer.impls.uninstall;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.installer.AbstractInstaller;
import net.kunmc.lab.kpm.installer.InstallResult;
import net.kunmc.lab.kpm.installer.impls.uninstall.signals.PluginIsDependencySignal;
import net.kunmc.lab.kpm.installer.impls.uninstall.signals.SearchingPluginSignal;
import net.kunmc.lab.kpm.installer.impls.uninstall.signals.UninstallReadySignal;
import net.kunmc.lab.kpm.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.kpm.installer.task.TaskFailedException;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.computer.DependsComputeOrderArgument;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.computer.DependsComputeOrderTask;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UnInstallResult;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UnInstallTask;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UninstallArgument;
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
    public PluginUninstaller(@NotNull KPMDaemon daemon, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(daemon, signalHandler);
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

        // region Check other plugins depends on this plugin.
        if (!argument.isSkipDependencyChecks())
        {
            for (Plugin plugin : installTargets)
            {
                List<Plugin> dependencies = this.getAllDependencies(plugin);
                dependencies.removeAll(installTargets);

                if (dependencies.isEmpty())
                    continue;
                PluginIsDependencySignal pluginIsDependencySignal =
                        new PluginIsDependencySignal(plugin, dependencies);

                this.postSignal(pluginIsDependencySignal);

                if (argument.isForceUninstall() || pluginIsDependencySignal.isForceUninstall())
                    installTargets.addAll(dependencies);
                else
                    return this.error(UnInstallErrorCause.PLUGIN_IS_DEPENDENCY);
            }

        }
        // endregion

        // endregion

        UninstallReadySignal uninstallReadySignal = new UninstallReadySignal(installTargets);
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

                    return new UninstallArgument(orderedPlugins);
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

    private ArrayList<Plugin> getAllDependencies(Plugin target)
    {
        ArrayList<Plugin> plugins = new ArrayList<>();

        List<DependencyNode> dependencies =
                this.daemon.getPluginMetaManager().getProvider().getDependedBy(target.getName());

        for (DependencyNode depend : dependencies)
        {
            Plugin dependPlugin = this.getPlugin(depend.getPlugin());
            if (dependPlugin != null)
            {
                plugins.add(dependPlugin);
                plugins.addAll(this.getAllDependencies(dependPlugin));
            }
            // if it cannot find the plugin, ignore it because it only exist in database.
            // Doesn't exist in filesystem and server plugins.
        }

        return plugins;
    }
}
