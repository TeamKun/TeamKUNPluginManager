package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.InstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals.DependsCacheSaveFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals.DependsCollectDependsDependsFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals.DependsDownloadFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals.DependsEnumeratedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals.DependsLoadDescriptionFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals.DependsResolveFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.download.DownloadResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolvePhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.resolve.PluginResolveResult;
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

public class DependsCollectPhase extends InstallPhase<DependsCollectArgument, DependsCollectResult>
{  // TODO: きれいに
    private final InstallerSignalHandler signalHandler;
    private final DependsCollectCache cache;

    private DependsCollectState phaseState;

    public DependsCollectPhase(@NotNull InstallProgress<?> progress, @NotNull InstallerSignalHandler signalHandler)
    {
        super(progress, signalHandler);

        this.signalHandler = signalHandler;
        this.cache = DependsCollectCache.of(progress.getInstallActionID().toString());

        this.phaseState = DependsCollectState.INITIALIZED;
    }

    @Override
    public @NotNull DependsCollectResult runPhase(@NotNull DependsCollectArgument arguments)
    {
        PluginDescriptionFile pluginDescription = arguments.getPluginDescription();
        this.cache.setPluginName(pluginDescription.getName());
        String pluginName = pluginDescription.getName();

        // Enumerate dependencies
        DependsEnumeratedSignal dependsSignal = new DependsEnumeratedSignal(
                pluginDescription.getDepend(),
                arguments.getAlreadyInstalledPlugins()
        );

        this.postSignal(dependsSignal);

        dependsSignal.getDependencies().forEach(this.cache::addDependency);

        HashMap<String, ResolveResult> resolvedResults;
        // region Resolve dependencies
        this.phaseState = DependsCollectState.RESOLVING_DEPENDS;

        resolvedResults = this.resolveDepends(dependsSignal.getDependencies(), arguments.getAlreadyInstalledPlugins());
        resolvedResults.entrySet().removeIf(entry -> !(entry.getValue() instanceof SuccessResult)); // Remove failures
        // endregion

        HashMap<String, DownloadResult> downloadResults;
        // region Download dependencies
        this.phaseState = DependsCollectState.DOWNLOADING_DEPENDS;

        downloadResults = this.downloadDepends(resolvedResults);
        downloadResults.entrySet().removeIf(entry -> !entry.getValue().isSuccess()); // Remove failures
        // endregion

        // region Collect dependency's dependencies (Recursive via collectDependsDepends)
        this.phaseState = DependsCollectState.COLLECTING_DEPENDS_DEPENDS;

        HashMap<String, PluginDescriptionFile> dependsDescriptions = downloadResultsToPluginDescriptionFiles(downloadResults);

        // Remove failed dependencies from load description results
        dependsDescriptions.entrySet().removeIf(entry -> entry.getValue() == null);

        dependsDescriptions.entrySet().stream().parallel()
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .filter(entry -> downloadResults.containsKey(entry.getKey()))
                .forEach(entry ->
                        this.cache.onCollect(entry.getValue().getName(), downloadResults.get(entry.getKey()).getPath())
                );

        if (!this.cache.save())
            this.postSignal(new DependsCacheSaveFailedSignal());

        //------------------------
        // Collect dependency's dependencies

        List<String> alreadyInstalled = new ArrayList<>(arguments.getAlreadyInstalledPlugins());
        alreadyInstalled.add(pluginName);
        alreadyInstalled.addAll(dependsDescriptions.keySet());

        this.collectDependsDepends(dependsDescriptions, alreadyInstalled);

        // endregion

        boolean success = this.cache.isErrors();
        DependsCollectErrorCause errorCause = success ? null: DependsCollectErrorCause.SOME_DEPENDENCIES_COLLECT_FAILED;

        return new DependsCollectResult(
                success, this.phaseState, errorCause,
                pluginName, this.cache.getCollectedDependencies(), this.cache.getCollectFailedDependencies()
        );
    }

    private DependsCollectResult passCollector(@NotNull PluginDescriptionFile pluginDescription,
                                               @NotNull List<String> alreadyCollectedPlugins)
    {
        DependsCollectArgument arguments = new DependsCollectArgument(pluginDescription, alreadyCollectedPlugins);

        return new DependsCollectPhase(this.progress, this.signalHandler) // do new() because DependsCollectPhase is stateful.
                .runPhase(arguments);
    }

    private void collectDependsDepends(@NotNull HashMap<String, PluginDescriptionFile> dependsDescriptions,
                                       @NotNull List<String> alreadyCollectedPlugins)
    {
        List<String> alreadyCollected = new ArrayList<>(alreadyCollectedPlugins);

        for (Map.Entry<String, PluginDescriptionFile> entry : dependsDescriptions.entrySet())
        {
            this.cache.save();

            DependsCollectResult dependsCollectResult = this.passCollector(entry.getValue(), alreadyCollected);

            this.cache.update();

            if (!dependsCollectResult.isSuccess())
            {
                this.postSignal(new DependsCollectDependsDependsFailedSignal(
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
                this.postSignal(new DependsLoadDescriptionFailedSignal(entry.getKey()));
            else
                pluginDescriptionFiles.put(entry.getKey(), pluginDescriptionFile);
        }

        return pluginDescriptionFiles;
    }

    private DownloadResult passDownloader(@NotNull String url)
    {
        DownloadArgument argument = new DownloadArgument(url);

        return new DownloadPhase(this.progress, this.signalHandler)
                .runPhase(argument);
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
                .forEach(entry -> this.postSignal(new DependsDownloadFailedSignal(entry.getKey())));

        return new HashMap<>(downloadResults);
    }

    private PluginResolveResult passResolver(@NotNull String dependency)
    {
        PluginResolveArgument resolveArgument = new PluginResolveArgument(dependency);

        return new PluginResolvePhase(this.progress, this.signalHandler)
                .runPhase(resolveArgument);
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
                .forEach(entry -> this.postSignal(new DependsResolveFailedSignal(entry.getKey())));

        return new HashMap<>(resolveResults);
    }
}
