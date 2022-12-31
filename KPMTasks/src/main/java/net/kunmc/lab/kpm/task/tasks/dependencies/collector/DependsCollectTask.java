package net.kunmc.lab.kpm.task.tasks.dependencies.collector;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;
import net.kunmc.lab.kpm.interfaces.installer.signals.InvalidKPMInfoFileSignal;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.kpminfo.InvalidInformationFileException;
import net.kunmc.lab.kpm.kpminfo.KPMInformationFile;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.task.AbstractInstallTask;
import net.kunmc.lab.kpm.task.tasks.dependencies.DependencyElement;
import net.kunmc.lab.kpm.task.tasks.dependencies.collector.signals.DependencyCollectDependencysDependsFailedSignal;
import net.kunmc.lab.kpm.task.tasks.dependencies.collector.signals.DependencyDownloadFailedSignal;
import net.kunmc.lab.kpm.task.tasks.dependencies.collector.signals.DependencyLoadDescriptionFailedSignal;
import net.kunmc.lab.kpm.task.tasks.dependencies.collector.signals.DependencyNameMismatchSignal;
import net.kunmc.lab.kpm.task.tasks.dependencies.collector.signals.DependencyResolveFailedSignal;
import net.kunmc.lab.kpm.task.tasks.dependencies.collector.signals.DependsCollectFailedSignal;
import net.kunmc.lab.kpm.task.tasks.dependencies.collector.signals.DependsDownloadFinishedSignal;
import net.kunmc.lab.kpm.task.tasks.dependencies.collector.signals.DependsEnumeratedSignal;
import net.kunmc.lab.kpm.task.tasks.download.DownloadArgument;
import net.kunmc.lab.kpm.task.tasks.download.DownloadResult;
import net.kunmc.lab.kpm.task.tasks.download.DownloadTask;
import net.kunmc.lab.kpm.task.tasks.resolve.PluginResolveArgument;
import net.kunmc.lab.kpm.task.tasks.resolve.PluginResolveResult;
import net.kunmc.lab.kpm.task.tasks.resolve.PluginResolveTask;
import net.kunmc.lab.kpm.utils.PluginUtil;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final KPMDaemon daemon;
    private final DependsCollectStatus status;

    private DependsCollectState taskState;

    public DependsCollectTask(@NotNull PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());

        this.daemon = installer.getDaemon();
        this.status = this.progress.getDependsCollectStatus();

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

        dependsSignal.getDependencies().stream().parallel()
                .filter(dependency -> !arguments.getAlreadyInstalledPlugins().contains(dependency))
                .forEach(this.status::addDependency);

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

        HashMap<String, DependencyElement> dependsDescriptions = this.downloadResultsToDependencyElement(downloadResults);

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

        this.collectDependsDepends(dependsDescriptions, alreadyInstalled);

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
                                               @NotNull List<String> alreadyCollectedPlugins)
    {
        DependsCollectArgument arguments = new DependsCollectArgument(pluginDescription, alreadyCollectedPlugins);

        return new DependsCollectTask(this.progress.getInstaller()) // do new() because DependsCollectTask is stateful.
                .runTask(arguments);
    }

    private void collectDependsDepends(@NotNull HashMap<String, DependencyElement> dependencies,
                                       @NotNull List<String> alreadyCollectedPlugins)
    {
        List<String> alreadyCollected = new ArrayList<>(alreadyCollectedPlugins);

        for (Map.Entry<String, DependencyElement> entry : dependencies.entrySet())
        {
            DependsCollectResult dependsCollectResult =
                    this.passCollector(entry.getValue().getPluginDescription(), alreadyCollected);

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
            e.printStackTrace();
            return null;
        }
    }

    private HashMap<String, @Nullable DependencyElement> downloadResultsToDependencyElement(
            @NotNull Map<String, DownloadResult> downloadResults)
    {
        HashMap<String, DependencyElement> dependencyElements = new HashMap<>();

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
                kpmInfoFile = this.daemon.getKpmInfoManager().loadInfo(entry.getValue().getPath(), pluginDescriptionFile);
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

            dependencyElements.put(entry.getKey(), new DependencyElement(
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

    private HashMap<String, DownloadResult> downloadDepends(@NotNull HashMap<String, ResolveResult> resolvedPlugins)
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

        HashMap<String, DownloadResult> downloadResultsCopy = new HashMap<>(downloadResults);

        this.postSignal(new DependsDownloadFinishedSignal((HashMap<String, DownloadResult>) downloadResults));

        return new HashMap<>(downloadResultsCopy);
    }

    private PluginResolveResult passResolver(@NotNull String dependency)
    {
        PluginResolveArgument resolveArgument = new PluginResolveArgument(dependency);

        return new PluginResolveTask(this.progress.getInstaller())
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
