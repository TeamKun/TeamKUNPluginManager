package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.InstallTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependencyCollectDependencysDependsFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependencyDownloadFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependencyLoadDescriptionFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependencyNameMismatchSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependencyResolveFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependsCollectFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.signals.DependsEnumeratedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.DownloadArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.DownloadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.download.DownloadTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.PluginResolveArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.PluginResolveResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.PluginResolveTask;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DependsCollectTask extends InstallTask<DependsCollectArgument, DependsCollectResult>
{  // TODO: きれいに
    private final InstallerSignalHandler signalHandler;
    private final DependsCollectStatus status;

    private DependsCollectState taskState;

    public DependsCollectTask(@NotNull InstallProgress<?> progress, @NotNull InstallerSignalHandler signalHandler)
    {
        super(progress, signalHandler);

        this.signalHandler = signalHandler;
        this.status = progress.getDependsCollectStatus();

        this.taskState = DependsCollectState.INITIALIZED;
    }

    @Override
    public @NotNull DependsCollectResult runTask(@NotNull DependsCollectArgument arguments)
    {
        PluginDescriptionFile pluginDescription = arguments.getPluginDescription();
        this.status.setPluginName(pluginDescription.getName());
        String pluginName = pluginDescription.getName();

        // Enumerate dependencies
        DependsEnumeratedSignal dependsSignal = new DependsEnumeratedSignal(
                pluginDescription.getDepend(),
                arguments.getAlreadyInstalledPlugins()
        );

        this.postSignal(dependsSignal);

        dependsSignal.getDependencies().forEach(this.status::addDependency);

        HashMap<String, ResolveResult> resolvedResults;
        // region Resolve dependencies
        this.taskState = DependsCollectState.RESOLVING_DEPENDS;

        resolvedResults = this.resolveDepends(dependsSignal.getDependencies(), arguments.getAlreadyInstalledPlugins());
        resolvedResults.entrySet().removeIf(entry -> !(entry.getValue() instanceof SuccessResult)); // Remove failures
        // endregion

        HashMap<String, DownloadResult> downloadResults;
        // region Download dependencies
        this.taskState = DependsCollectState.DOWNLOADING_DEPENDS;

        downloadResults = this.downloadDepends(resolvedResults);
        downloadResults.entrySet().removeIf(entry -> !entry.getValue().isSuccess()); // Remove failures
        // endregion

        // region Collect dependency's dependencies (Recursive via collectDependsDepends)
        this.taskState = DependsCollectState.COLLECTING_DEPENDS_DEPENDS;

        HashMap<String, PluginDescriptionFile> dependsDescriptions = downloadResultsToPluginDescriptionFiles(downloadResults);

        // Remove failed dependencies from load description results
        dependsDescriptions.entrySet().removeIf(entry -> entry.getValue() == null);

        dependsDescriptions.entrySet().stream().parallel()
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .filter(entry -> downloadResults.containsKey(entry.getKey()))
                .forEach(entry -> {
                    String exceptedName = entry.getValue().getName();
                    String actualName = entry.getKey();
                    if (!exceptedName.equals(actualName))
                    {
                        this.postSignal(new DependencyNameMismatchSignal(actualName, exceptedName));
                        return;
                    }

                    Path pluginPath = downloadResults.get(actualName).getPath();

                    assert pluginPath != null;
                    this.status.onCollect(exceptedName, new DependencyElement(exceptedName, pluginPath, entry.getValue()));
                });

        //------------------------
        // Collect dependency's dependencies

        List<String> alreadyInstalled = new ArrayList<>(arguments.getAlreadyInstalledPlugins());
        alreadyInstalled.add(pluginName);
        alreadyInstalled.addAll(dependsDescriptions.keySet());

        this.collectDependsDepends(dependsDescriptions, alreadyInstalled);

        // endregion

        boolean success = this.status.isErrors();
        DependsCollectErrorCause errorCause = success ? null: DependsCollectErrorCause.SOME_DEPENDENCIES_COLLECT_FAILED;

        List<String> collectFailedDependencies = this.status.getCollectFailedDependencies();
        if (!success)
            this.postSignal(new DependsCollectFailedSignal(collectFailedDependencies));

        return new DependsCollectResult(
                success, this.taskState, errorCause,
                pluginName, this.status.getCollectedDependencies(), collectFailedDependencies
        );
    }

    private DependsCollectResult passCollector(@NotNull PluginDescriptionFile pluginDescription,
                                               @NotNull List<String> alreadyCollectedPlugins)
    {
        DependsCollectArgument arguments = new DependsCollectArgument(pluginDescription, alreadyCollectedPlugins);

        return new DependsCollectTask(this.progress, this.signalHandler) // do new() because DependsCollectTask is stateful.
                .runTask(arguments);
    }

    private void collectDependsDepends(@NotNull HashMap<String, PluginDescriptionFile> dependsDescriptions,
                                       @NotNull List<String> alreadyCollectedPlugins)
    {
        List<String> alreadyCollected = new ArrayList<>(alreadyCollectedPlugins);

        for (Map.Entry<String, PluginDescriptionFile> entry : dependsDescriptions.entrySet())
        {
            DependsCollectResult dependsCollectResult = this.passCollector(entry.getValue(), alreadyCollected);

            if (!dependsCollectResult.isSuccess())
            {
                this.postSignal(new DependencyCollectDependencysDependsFailedSignal(
                        dependsCollectResult.getTargetPlugin(),
                        dependsCollectResult.getCollectFailedPlugins()
                ));
            }
            else
                alreadyCollected.add(entry.getKey());
        }
    }

    private PluginDescriptionFile downloadResultToPluginDescriptionFile(@NotNull DownloadResult downloadResult)
    {
        try
        {
            Path pluginPath = downloadResult.getPath();

            if (pluginPath == null)
                return null;

            return PluginUtil.loadDescription(pluginPath.toFile());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private HashMap<String, @Nullable PluginDescriptionFile> downloadResultsToPluginDescriptionFiles(
            @NotNull Map<String, DownloadResult> downloadResults)
    {
        HashMap<String, PluginDescriptionFile> pluginDescriptionFiles = new HashMap<>();

        for (Map.Entry<String, DownloadResult> entry : downloadResults.entrySet())
        {
            PluginDescriptionFile pluginDescriptionFile = this.downloadResultToPluginDescriptionFile(entry.getValue());

            if (pluginDescriptionFile == null)
                this.postSignal(new DependencyLoadDescriptionFailedSignal(entry.getKey()));
            else
                pluginDescriptionFiles.put(entry.getKey(), pluginDescriptionFile);
        }

        return pluginDescriptionFiles;
    }

    private DownloadResult passDownloader(@NotNull String url)
    {
        DownloadArgument argument = new DownloadArgument(url);

        return new DownloadTask(this.progress, this.signalHandler)
                .runTask(argument);
    }

    private HashMap<String, DownloadResult> downloadDepends(@NotNull HashMap<String, ResolveResult> resolvedPlugins)
    {
        Map<String, DownloadResult> downloadResults = resolvedPlugins.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    SuccessResult successResult = (SuccessResult) entry.getValue();
                    return passDownloader(successResult.getDownloadUrl());  // Actual downloading
                }));

        downloadResults.entrySet().stream()
                .filter(entry -> !entry.getValue().isSuccess())
                .forEach(entry -> this.postSignal(new DependencyDownloadFailedSignal(entry.getKey())));

        return new HashMap<>(downloadResults);
    }

    private PluginResolveResult passResolver(@NotNull String dependency)
    {
        PluginResolveArgument resolveArgument = new PluginResolveArgument(dependency);

        return new PluginResolveTask(this.progress, this.signalHandler)
                .runTask(resolveArgument);
    }

    private HashMap<String, ResolveResult> resolveDepends(@NotNull List<String> dependencies,
                                                          @NotNull List<String> alreadyInstalledPlugins)
    {
        Map<String, ResolveResult> resolveResults = new HashMap<>(dependencies.stream()
                .filter(dependency -> !alreadyInstalledPlugins.contains(dependency))
                .map(dependency -> new Pair<>(
                        dependency,
                        this.passResolver(dependency).getResolveResult()  // Actual resolving
                ))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));

        resolveResults.entrySet().stream()
                .filter(entry -> !(entry.getValue() instanceof SuccessResult))
                .forEach(entry -> this.postSignal(new DependencyResolveFailedSignal(entry.getKey())));

        return new HashMap<>(resolveResults);
    }
}
