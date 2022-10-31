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
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UnInstallArgument;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UnInstallResult;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.UnInstallTask;
import net.kunmc.lab.kpm.meta.DependencyNode;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.utils.PluginUtil;
import net.kunmc.lab.kpm.utils.ReversedCollector;
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
public class PluginUninstaller extends AbstractInstaller<UninstallArgument, UnInstallErrorCause, UnInstallTasks>
{
    public PluginUninstaller(@NotNull KPMDaemon daemon, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(daemon, signalHandler);
    }

    @Override
    public InstallResult<UnInstallTasks> execute(@NotNull UninstallArgument argument) throws TaskFailedException
    {
        List<Plugin> plugins = new ArrayList<>();
        // region Search plugin
        this.progress.setCurrentTask(UnInstallTasks.SEARCHING_PLUGIN);

        for (String pluginName : argument.getPlugins())
        {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin == null)
                return this.error(UnInstallErrorCause.PLUGIN_NOT_FOUND);

            plugins.add(plugin);
        }


        // endregion

        // region Do assertions.
        this.progress.setCurrentTask(UnInstallTasks.CHECKING_ENVIRONMENT);

        // region Check is plugin marked as ignored
        for (Plugin plugin : plugins)
        {
            if (this.isPluginIgnored(plugin.getName()))
            {
                IgnoredPluginSignal ignoredPluginSignal = new IgnoredPluginSignal(plugin.getDescription());
                this.postSignal(ignoredPluginSignal);
                if (ignoredPluginSignal.isCancelInstall())
                    return this.error(UnInstallErrorCause.PLUGIN_IGNORED);
            }
        }

        // endregion

        // region Check plugin are depended by other plugins

        for (Plugin plugin : plugins)
        {
            List<Plugin> dependencies = this.getAllDependencies(plugin);
            dependencies.removeAll(plugins);

            if (!dependencies.isEmpty())
            {
                PluginIsDependencySignal pluginIsDependencySignal =
                        new PluginIsDependencySignal(plugin, dependencies);

                this.postSignal(pluginIsDependencySignal);

                if (pluginIsDependencySignal.isForceUninstall())
                    plugins.addAll(dependencies);
                else
                    return this.error(UnInstallErrorCause.PLUGIN_IS_DEPENDENCY);
            }
        }

        // endregion

        // endregion

        UninstallReadySignal uninstallReadySignal = new UninstallReadySignal(plugins);
        this.postSignal(uninstallReadySignal);
        if (!uninstallReadySignal.isContinueUninstall())
            return this.error(UnInstallErrorCause.CANCELLED);

        plugins = uninstallReadySignal.getPlugins();

        // region Uninstall plugin

        // Before uninstall plugin, we need to compute the order of plugins to uninstall.
        // Reuse DepenedsComputeOrderTask to compute the order, so we need to map List<Plugin> to List<DependencyElement>.
        List<DependencyElement> computeOrderTarget = plugins.stream().parallel()
                .map(pl -> {
                    PluginDescriptionFile descriptionFile = pl.getDescription();
                    String name = descriptionFile.getName();
                    Path path = PluginUtil.getFile(pl).toPath();

                    // Dummy element
                    return new DependencyElement(name, path, descriptionFile, null, null);
                })
                .collect(Collectors.toList());

        // Create String-Plugin map to DependencyElement doesn't have Plugin instance.
        Map<String, Plugin> namePluginMap = plugins.stream().parallel()
                .collect(Collectors.toMap(Plugin::getName, pl -> pl));

        UnInstallResult uninstallResult = (UnInstallResult)
                this.submitter(
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
                                    .collect(ReversedCollector.toList()); // Convert load order to unload order.

                            return new UnInstallArgument(orderedPlugins);
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
                this.daemon.getPluginMetaManager().getProvider().getDependOn(target.getName());

        for (DependencyNode depend : dependencies)
        {
            Plugin dependPlugin = this.getPlugin(depend.getDependsOn());
            if (dependPlugin != null)
            {
                plugins.add(dependPlugin);
                plugins.addAll(this.getAllDependencies(dependPlugin));
            }
            // if depend plugin not found, ignore it because it only exist in database.
            // Doesn't exist in filesystem and server plugins.
        }

        return plugins;
    }
}
