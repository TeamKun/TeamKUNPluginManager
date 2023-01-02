package net.kunmc.lab.kpm.installer.impls.clean;

import net.kunmc.lab.kpm.KPMRegistry;
import net.kunmc.lab.kpm.installer.AbstractInstaller;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.task.TaskFailedException;
import net.kunmc.lab.kpm.task.tasks.garbage.clean.GarbageCleanArgument;
import net.kunmc.lab.kpm.task.tasks.garbage.clean.GarbageCleanResult;
import net.kunmc.lab.kpm.task.tasks.garbage.clean.GarbageCleanTask;
import net.kunmc.lab.kpm.task.tasks.garbage.search.GarbageSearchArgument;
import net.kunmc.lab.kpm.task.tasks.garbage.search.GarbageSearchTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * プラグインの不要なデータを削除するインストーラーの実装です。
 * 不要なデータの削除は主に以下の流れで動作します。
 *
 * <ol>
 *     <li>{@link CleanTasks#SEARCHING_GARBAGE} - 不要なデータを検索する。</li>
 *     <li>{@link CleanTasks#DELETING_GARBAGE} - 不要なデータを削除する。</li>
 * </ol>
 */
public class GarbageCleaner extends AbstractInstaller<CleanArgument, CleanErrorCause, CleanTasks>
{

    public GarbageCleaner(@NotNull KPMRegistry registry, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(registry, signalHandler);
    }

    @Override
    public InstallResult<CleanTasks> execute(@NotNull CleanArgument argument) throws TaskFailedException
    {
        Path pluginsDir = this.registry.getEnvironment().getPlugin().getDataFolder().getParentFile().toPath();
        List<String> excludeDataNames = argument.getExcludeDataNames();
        excludeDataNames.addAll(this.registry.getEnvironment().getExcludes());
        List<String> plugins = Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                .map(Plugin::getName)
                .collect(Collectors.toList());

        GarbageCleanResult result = this.submitter(
                        CleanTasks.SEARCHING_GARBAGE,
                        new GarbageSearchTask(this)
                )
                .then(
                        CleanTasks.DELETING_GARBAGE,
                        new GarbageCleanTask(this)
                )
                .bridgeArgument(searchResult ->
                        new GarbageCleanArgument(searchResult.getGarbageFiles()))
                .submitAll(new GarbageSearchArgument(excludeDataNames, pluginsDir, plugins));

        List<Path> deleted = new ArrayList<>();
        List<Path> failed = new ArrayList<>();

        result.getDeletedGarbage().forEach((key, value) -> {
            if (value)
            {
                deleted.add(key);
                this.progress.addRemoved(key.getFileName().toString());
            }
            else
                failed.add(key);
        });

        return this.success(new GarbageCleanSucceedResult(this.progress, deleted, failed));
    }
}
