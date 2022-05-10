package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.InstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals.DependsDescriptionLoadFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals.DependsDownloadFailedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends.signals.DependsEnumeratedSignal;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DependsCollectPhase extends InstallPhase<DependsCollectArgument, DependsCollectResult>
{  // TODO: きれいに
    private final InstallerSignalHandler signalHandler;

    private DependsCollectState phaseState;

    public DependsCollectPhase(@NotNull InstallProgress<?> progress, @NotNull InstallerSignalHandler signalHandler)
    {
        super(progress, signalHandler);

        this.signalHandler = signalHandler;
        this.phaseState = DependsCollectState.INITIALIZED;
    }

    private static HashMap<String, List<String>> convertStringHashSetToStringListHashMap(HashMap<String, HashSet<String>> hashSetHashMap)
    {
        HashMap<String, List<String>> listHashMap = new HashMap<>();
        for (String key : hashSetHashMap.keySet())
            listHashMap.put(key, new ArrayList<>(hashSetHashMap.get(key)));

        return listHashMap;
    }

    @Override
    public @NotNull DependsCollectResult runPhase(@NotNull DependsCollectArgument arguments)
    {
        PluginDescriptionFile pluginDescription = arguments.getPluginDescription();
        String pluginName = pluginDescription.getName();
        HashSet<String> collectingFailedDepends;

        // Enumerate dependencies
        DependsEnumeratedSignal dependsSignal = new DependsEnumeratedSignal(
                pluginDescription.getDepend(),
                arguments.getAlreadyInstalledPlugins()
        );

        this.postSignal(dependsSignal);

        HashMap<String, ResolveResult> resolvedResults;
        // region Resolve dependencies
        this.phaseState = DependsCollectState.RESOLVING_DEPENDS;

        resolvedResults = this.resolveDepends(dependsSignal.getDependencies(), arguments.getAlreadyInstalledPlugins());
        collectingFailedDepends = new HashSet<>(this.indicateResolveErrors(resolvedResults)); // Post signal on failed

        // Remove failed dependencies from resolve results
        collectingFailedDepends.stream().parallel().forEach(resolvedResults::remove);
        // endregion

        HashMap<String, DownloadResult> downloadResults;
        // region Download dependencies
        this.phaseState = DependsCollectState.DOWNLOADING_DEPENDS;

        downloadResults = this.downloadDepends(resolvedResults);

        List<String> downloadFailures = this.indicateDownloadErrors(downloadResults); // Post signal on failed
        collectingFailedDepends.addAll(downloadFailures);

        // Remove failed dependencies from download results
        downloadFailures.stream().parallel().forEach(downloadResults::remove);
        // endregion

        HashMap<String, HashSet<String>> collectFailures;
        // region Collect dependency's dependencies (Recursive via collectDependsDepends)
        this.phaseState = DependsCollectState.COLLECTING_DEPENDS_DEPENDS;

        HashMap<String, PluginDescriptionFile> dependsDescriptions = downloadResultsToPluginDescriptionFiles(downloadResults);

        List<String> loadDescriptionsFailures = this.indicateLoadDescriptionErrors(dependsDescriptions); // Post signal on failed
        collectingFailedDepends.addAll(loadDescriptionsFailures);

        // Remove failed dependencies from load description results
        loadDescriptionsFailures.stream().parallel().forEach(dependsDescriptions::remove);

        //------------------------
        // Collect dependency's dependencies

        List<String> alreadyInstalled = new ArrayList<>(arguments.getAlreadyInstalledPlugins());
        alreadyInstalled.add(pluginName);
        alreadyInstalled.addAll(dependsDescriptions.keySet());


        HashMap<String, DependsCollectResult> dependsCollectResults =
                this.collectDependsDepends(dependsDescriptions, alreadyInstalled);

        // Post signal on failed
        collectFailures = this.indicateCollectDependsDependsErrors(dependsCollectResults);


        // endregion

        if (!collectFailures.containsKey(pluginName))
            collectFailures.put(pluginName, new HashSet<>());
        collectFailures.get(pluginName).addAll(collectingFailedDepends);

        boolean success = collectingFailedDepends.isEmpty();
        DependsCollectErrorCause errorCause = success ? null: DependsCollectErrorCause.SOME_DEPENDENCIES_COLLECT_FAILED;

        return new DependsCollectResult(
                success, this.phaseState, errorCause,
                pluginName, convertStringHashSetToStringListHashMap(collectFailures)
        );
    }

    private HashMap<String, HashSet<String>> indicateCollectDependsDependsErrors(@NotNull HashMap<String, DependsCollectResult> dependsCollectResults)
    { // Not same as other indicate methods
        HashMap<String, HashSet<String>> collectingFailedDepends = new HashMap<>();

        for (Map.Entry<String, DependsCollectResult> entry : dependsCollectResults.entrySet())
        {
            DependsCollectResult dependsCollectResult = entry.getValue();

            if (!dependsCollectResult.isSuccess())
                for (Map.Entry<String, List<String>> entry2 : dependsCollectResult.getCollectingFailedPlugins().entrySet())
                {
                    String pluginName = entry2.getKey();
                    List<String> collectingFailedDependsList = entry2.getValue();
                    if (!collectingFailedDepends.containsKey(pluginName))
                        collectingFailedDepends.put(pluginName, new HashSet<>());

                    collectingFailedDepends.get(pluginName).addAll(collectingFailedDependsList);
                }
        }

        return collectingFailedDepends;
    }

    private DependsCollectResult passCollector(@NotNull PluginDescriptionFile pluginDescription,
                                               @NotNull List<String> alreadyCollectedPlugins)
    {
        DependsCollectArgument arguments = new DependsCollectArgument(pluginDescription, alreadyCollectedPlugins);

        return new DependsCollectPhase(this.progress, this.signalHandler) // do new() because DependsCollectPhase is stateful.
                .runPhase(arguments);
    }

    private HashMap<String, DependsCollectResult> collectDependsDepends(@NotNull HashMap<String, PluginDescriptionFile> dependsDescriptions,
                                                                        @NotNull List<String> alreadyCollectedPlugins)
    {
        return new HashMap<>(dependsDescriptions.entrySet().stream()
                .parallel()
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        this.passCollector(entry.getValue(), alreadyCollectedPlugins))
                ));
    }

    private List<String> indicateLoadDescriptionErrors(@NotNull HashMap<String, PluginDescriptionFile> dependsDescriptions)
    {
        List<String> errors = (dependsDescriptions.entrySet().stream())
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!errors.isEmpty())
            this.postSignal(new DependsDescriptionLoadFailedSignal(errors));

        return errors;
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
        return new HashMap<>(resolvedPlugins.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    SuccessResult successResult = (SuccessResult) entry.getValue();
                    return passDownloader(successResult.getDownloadUrl());  // Actual downloading
                })));
    }

    private List<String> indicateDownloadErrors(@NotNull HashMap<String, DownloadResult> downloadResults)
    {
        List<String> errors = (downloadResults.entrySet().stream())
                .filter(entry -> !entry.getValue().isSuccess())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!errors.isEmpty())
            this.postSignal(new DependsDownloadFailedSignal(errors));

        return errors;
    }

    private List<String> indicateResolveErrors(@NotNull HashMap<String, ResolveResult> resolveResults)
    {
        List<String> errors = resolveResults.entrySet().stream().parallel()
                .filter(entry -> !(entry.getValue() instanceof SuccessResult))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!errors.isEmpty())
            this.postSignal(new DependsResolveFailedSignal(errors));

        return errors;
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
        return new HashMap<>(dependencies.stream()
                .filter(dependency -> !alreadyInstalledPlugins.contains(dependency))
                .map(dependency -> new Pair<>(
                        dependency,
                        this.passResolver(dependency).getResolveResult()  // Actual resolving
                ))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
    }
}
