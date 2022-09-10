package net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall;

import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.signals.PluginIsDependencySignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.signals.SearchingPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskFailedException;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.computer.DependsComputeOrderArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.computer.DependsComputeOrderTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.UnInstallArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.UnInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.UnInstallTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandleManager;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.kunmc.lab.teamkunpluginmanager.utils.ReversedCollector;
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
public class PluginUninstaller extends AbstractInstaller<UnInstallErrorCause, UnInstallTasks>
{
    public PluginUninstaller(SignalHandleManager signalHandler) throws IOException
    {
        super(signalHandler);
    }

    @Override
    public InstallResult<UnInstallTasks> execute(@NotNull String query) throws TaskFailedException
    {
        List<Plugin> plugins = new ArrayList<>();
        // region Search plugin
        this.progress.setCurrentTask(UnInstallTasks.SEARCHING_PLUGIN);

        Plugin plugin = this.getPlugin(query);
        if (plugin == null)
            return this.error(UnInstallErrorCause.PLUGIN_NOT_FOUND);

        plugins.add(plugin);


        // endregion

        // region Do assertions.
        this.progress.setCurrentTask(UnInstallTasks.CHECKING_ENVIRONMENT);

        // region Check is plugin marked as ignored
        if (this.isPluginIgnored(plugin.getName()))
        {
            IgnoredPluginSignal ignoredPluginSignal = new IgnoredPluginSignal(plugin.getDescription());
            this.postSignal(ignoredPluginSignal);
            if (ignoredPluginSignal.isCancelInstall())
                return this.error(UnInstallErrorCause.PLUGIN_IGNORED);
        }

        // endregion

        // region Check plugin are depended by other plugins

        List<Plugin> dependencies = this.getAllDependencies(plugin);

        if (!dependencies.isEmpty())
        {
            PluginIsDependencySignal pluginIsDependencySignal =
                    new PluginIsDependencySignal(plugin.getName(), dependencies);

            this.postSignal(pluginIsDependencySignal);

            if (pluginIsDependencySignal.isForceUninstall())
                plugins.addAll(dependencies);
            else
                return this.error(UnInstallErrorCause.PLUGIN_IS_DEPENDENCY);
        }

        // endregion

        // endregion

        // region Uninstall plugin

        // Before uninstall plugin, we need to compute the order of plugins to uninstall.
        // Reuse DepenedsComputeOrderTask to compute the order, so we need to map List<Plugin> to List<DependencyElement>.
        List<DependencyElement> computeOrderTarget = plugins.stream().parallel()
                .map(pl -> {
                    PluginDescriptionFile descriptionFile = pl.getDescription();
                    String name = descriptionFile.getName();
                    Path path = PluginUtil.getFile(pl).toPath();

                    return new DependencyElement(name, path, descriptionFile);
                })
                .collect(Collectors.toList());

        // Create String-Plugin map to DependencyElement doesn't have Plugin instance.
        Map<String, Plugin> namePluginMap = plugins.stream().parallel()
                .collect(Collectors.toMap(Plugin::getName, pl -> pl));

        UnInstallResult uninstallResult = (UnInstallResult)
                this.submitter(
                                UnInstallTasks.COMPUTING_UNINSTALL_ORDER,
                                new DependsComputeOrderTask(this.progress, this.signalHandler)
                        )
                        .then(
                                UnInstallTasks.UNINSTALLING_PLUGINS,
                                new UnInstallTask(this.progress, this.signalHandler)
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

        return this.success();
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

        DependencyTree.Info info = DependencyTree.getInfo(target.getName(), false);
        if (info == null)
            return new ArrayList<>();

        for (DependencyTree.Info.Depend depend : info.rdepends)
        {
            Plugin dependPlugin = this.getPlugin(depend.depend);
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