package org.kunlab.kpm.task.tasks.dependencies.collector;

import net.kunmc.lab.peyangpaperutils.collectors.ExCollectors;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.installer.interfaces.Installer;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.installer.signals.InvalidKPMInfoFileSignal;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.kpminfo.InvalidInformationFileException;
import org.kunlab.kpm.kpminfo.KPMInformationFile;
import org.kunlab.kpm.resolver.QueryContextParser;
import org.kunlab.kpm.resolver.interfaces.QueryContext;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;
import org.kunlab.kpm.resolver.interfaces.result.SuccessResult;
import org.kunlab.kpm.task.AbstractInstallTask;
import org.kunlab.kpm.task.interfaces.dependencies.DependencyElement;
import org.kunlab.kpm.task.interfaces.dependencies.collector.DependsCollectStatus;
import org.kunlab.kpm.task.tasks.dependencies.DependencyElementImpl;
import org.kunlab.kpm.task.tasks.dependencies.collector.signals.*;
import org.kunlab.kpm.task.tasks.download.DownloadArgument;
import org.kunlab.kpm.task.tasks.download.DownloadResult;
import org.kunlab.kpm.task.tasks.download.DownloadTask;
import org.kunlab.kpm.task.tasks.resolve.PluginResolveArgument;
import org.kunlab.kpm.task.tasks.resolve.PluginResolveResult;
import org.kunlab.kpm.task.tasks.resolve.PluginResolveTask;
import org.kunlab.kpm.utils.PluginUtil;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 依存関係解決タスクです。
 * このタスクは、以下のシグナルをスローします：
 * <ul>
 *     <li>{@link DependsEnumeratedSignal}</li>
 *     <li>{@link PluginResolveTask} からスローされる可能性のあるシグナル</li>
 *     <li>{@link DownloadTask} からスローされる可能性のあるシグナル</li>
 *     <li>{@link DependencyLoadDescriptionFailedSignal}</li>
 *     <li>{@link DependencyNameMismatchSignal}</li>
 *     <li>{@link DependencyDownloadFailedSignal}</li>
 *     <li>{@link DependencyResolveFailedSignal}</li>
 *     <li>{@link DependencyCollectDependencysDependsFailedSignal}</li>
 *     <li>{@link DependsCollectFailedSignal}</li>
 * </ul>
 */
public class DependsCollectTask extends AbstractInstallTask<DependsCollectArgument, DependsCollectResult>
{  // TODO: きれいに
    private final KPMRegistry registry;
    private final DependsCollectStatus status;

    private DependsCollectState taskState;

    public DependsCollectTask(@NotNull Installer<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());

        this.registry = installer.getRegistry();
        this.status = this.progress.getDependsCollectStatus();

        this.taskState = DependsCollectState.INITIALIZED;
    }

    private static Map<String, QueryContext> buildQueryContext(@NotNull List<String> dependencyNames,
                                                               @NotNull Map<String, QueryContext> sources)
    {
        Map<String, QueryContext> results = new HashMap<>();

        for (String dependencyName : dependencyNames)
        {
            QueryContext context = sources.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(dependencyName))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(QueryContextParser.fromString(dependencyName));
            results.put(dependencyName, context);
        }

        return results;
    }

    @Override
    public @NotNull DependsCollectResult runTask(@NotNull DependsCollectArgument arguments)
    {
        PluginDescriptionFile pluginDescription = arguments.getPluginDescription();
        this.status.setPluginName(pluginDescription.getName());
        String pluginName = pluginDescription.getName();

        Map<String, QueryContext> sources = buildQueryContext(pluginDescription.getDepend(), arguments.getSources());

        // Enumerate dependencies
        DependsEnumeratedSignal dependsSignal = new DependsEnumeratedSignal(
                sources,
                arguments.getAlreadyInstalledPlugins()
        );

        this.postSignal(dependsSignal);

        dependsSignal.getDependencies().keySet().stream().parallel()
                .filter(dependency -> !arguments.getAlreadyInstalledPlugins().contains(dependency))
                .forEach(this.status::addDependency);

        Map<String, ResolveResult> resolvedResults;
        // region Resolve dependencies
        this.taskState = DependsCollectState.RESOLVING_DEPENDS;

        resolvedResults = this.resolveDepends(sources, arguments.getAlreadyInstalledPlugins());
        resolvedResults.entrySet().removeIf(entry -> !(entry.getValue() instanceof SuccessResult)); // Remove failures
        // endregion

        Map<String, DownloadResult> downloadResults;
        // region Download dependencies
        this.taskState = DependsCollectState.DOWNLOADING_DEPENDS;

        downloadResults = this.downloadDepends(resolvedResults);
        downloadResults.entrySet().removeIf(entry -> !entry.getValue().isSuccess()); // Remove failures
        // endregion

        // region Collect dependency's dependencies (Recursive via collectDependsDepends)
        this.taskState = DependsCollectState.COLLECTING_DEPENDS_DEPENDS;

        Map<String, DependencyElement> dependsDescriptions = this.downloadResultsToDependencyElement(downloadResults);

        // Remove failed dependencies from load description results
        dependsDescriptions.entrySet().removeIf(entry -> entry.getValue() == null);

        dependsDescriptions.entrySet().stream()
                .filter(entry -> downloadResults.containsKey(entry.getKey()))
                .forEach((entry) -> {
                    String exceptedName = entry.getValue().getPluginName();
                    String actualName = entry.getKey();
                    if (!exceptedName.equals(actualName))
                    {
                        this.postSignal(new DependencyNameMismatchSignal(actualName, exceptedName));
                        return;
                    }

                    this.status.onCollect(
                            exceptedName,
                            entry.getValue()
                    );
                });

        //------------------------
        // Collect dependency's dependencies

        List<String> alreadyInstalled = new ArrayList<>(arguments.getAlreadyInstalledPlugins());
        alreadyInstalled.add(pluginName);
        alreadyInstalled.addAll(dependsDescriptions.keySet());

        this.collectDependsDepends(dependsDescriptions, alreadyInstalled, sources);

        // endregion

        boolean success = !this.status.isErrors();
        DependsCollectErrorCause errorCause = success ? null: DependsCollectErrorCause.SOME_DEPENDENCIES_COLLECT_FAILED;

        List<String> collectFailedDependencies = this.status.getCollectFailedDependencies();
        if (!success)
            this.postSignal(new DependsCollectFailedSignal(collectFailedDependencies));

        return new DependsCollectResult(this.taskState, errorCause, pluginName,
                this.status.getCollectedDependencies(), collectFailedDependencies
        );
    }

    private DependsCollectResult passCollector(@NotNull PluginDescriptionFile pluginDescription,
                                               @NotNull Map<String, QueryContext> sources,
                                               @NotNull List<String> alreadyCollectedPlugins)
    {
        DependsCollectArgument arguments =
                new DependsCollectArgument(pluginDescription, sources, alreadyCollectedPlugins);

        return new DependsCollectTask(this.progress.getInstaller()) // do new() because DependsCollectTask is stateful.
                .runTask(arguments);
    }

    private void collectDependsDepends(@NotNull Map<String, DependencyElement> dependencies,
                                       @NotNull List<String> alreadyCollectedPlugins,
                                       @NotNull Map<String, ? extends QueryContext> parentSources)
    {
        List<String> alreadyCollected = new ArrayList<>(alreadyCollectedPlugins);

        for (Map.Entry<String, DependencyElement> entry : dependencies.entrySet())
        {
            DependencyElement dependency = entry.getValue();
            KPMInformationFile informationFile = dependency.getKpmInfoFile();
            Map<String, QueryContext> sources = new HashMap<>(parentSources);

            if (informationFile != null)
                sources.putAll(informationFile.getDependencies());

            DependsCollectResult dependsCollectResult =
                    this.passCollector(dependency.getPluginDescription(), sources, alreadyCollected);

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

            return PluginUtil.loadDescription(pluginPath.toFile());
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
            return null;
        }
    }

    private Map<String, @Nullable DependencyElement> downloadResultsToDependencyElement(
            @NotNull Map<String, DownloadResult> downloadResults)
    {
        Map<String, DependencyElement> dependencyElements = new HashMap<>();

        for (Map.Entry<String, DownloadResult> entry : downloadResults.entrySet())
        {
            PluginDescriptionFile pluginDescriptionFile = this.downloadResultToPluginDescriptionFile(entry.getValue());
            if (pluginDescriptionFile == null)
            {
                this.postSignal(new DependencyLoadDescriptionFailedSignal(entry.getKey()));
                continue;
            }

            KPMInformationFile kpmInfoFile = null;
            try
            {
                kpmInfoFile = this.registry.getKpmInfoManager().loadInfo(entry.getValue().getPath(), pluginDescriptionFile);
            }
            catch (InvalidInformationFileException ex)
            {
                InvalidKPMInfoFileSignal signal =
                        new InvalidKPMInfoFileSignal(entry.getValue().getPath(), pluginDescriptionFile);
                this.postSignal(signal);
                if (!signal.isIgnore())
                    continue;
            }
            catch (FileNotFoundException ignored)
            {
            }

            String query;
            if (kpmInfoFile != null && kpmInfoFile.getUpdateQuery() != null)
                query = kpmInfoFile.getUpdateQuery().getQuery();
            else
                query = pluginDescriptionFile.getName();

            dependencyElements.put(entry.getKey(), new DependencyElementImpl(
                    pluginDescriptionFile.getName(), entry.getValue().getPath(),
                    pluginDescriptionFile, kpmInfoFile, query
            ));
        }

        return dependencyElements;
    }

    private DownloadResult passDownloader(@NotNull String url)
    {
        DownloadArgument argument = new DownloadArgument(url);

        return new DownloadTask(this.progress.getInstaller())
                .runTask(argument);
    }

    private Map<String, DownloadResult> downloadDepends(@NotNull Map<String, ResolveResult> resolvedPlugins)
    {
        Map<String, DownloadResult> downloadResults = resolvedPlugins.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    SuccessResult successResult = (SuccessResult) entry.getValue();
                    return this.passDownloader(successResult.getDownloadUrl());  // Actual downloading
                }));

        downloadResults.entrySet().stream()
                .filter(entry -> !entry.getValue().isSuccess())
                .forEach(entry -> this.postSignal(
                        new DependencyDownloadFailedSignal(entry.getKey(), entry.getValue().getUrl())));

        Map<String, DownloadResult> downloadResultsCopy = new HashMap<>(downloadResults);

        this.postSignal(new DependsDownloadFinishedSignal(downloadResults));

        return new HashMap<>(downloadResultsCopy);
    }

    private PluginResolveResult passResolver(@NotNull QueryContext query)
    {
        PluginResolveArgument resolveArgument = new PluginResolveArgument(query.toString());

        return new PluginResolveTask(this.progress.getInstaller())
                .runTask(resolveArgument);
    }

    private Map<String, ResolveResult> resolveDepends(@NotNull Map<String, ? extends QueryContext> dependencies,
                                                      @NotNull List<String> alreadyInstalledPlugins)
    {
        Map<String, ResolveResult> resolveResults = new HashMap<>(dependencies.entrySet().stream()
                .filter(dependency -> !alreadyInstalledPlugins.contains(dependency.getKey()))
                .map(dependency -> Pair.of(
                        dependency.getKey(),
                        this.passResolver(dependency.getValue()).getResolveResult()
                ))
                .collect(ExCollectors.toPairHashMap()));

        resolveResults.entrySet().stream()
                .filter(entry -> !(entry.getValue() instanceof SuccessResult))
                .forEach(entry -> this.postSignal(new DependencyResolveFailedSignal(entry.getKey())));

        return new HashMap<>(resolveResults);
    }
}
