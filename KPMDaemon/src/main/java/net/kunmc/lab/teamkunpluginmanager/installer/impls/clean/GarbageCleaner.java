package net.kunmc.lab.teamkunpluginmanager.installer.impls.clean;

import net.kunmc.lab.teamkunpluginmanager.KPMDaemon;
import net.kunmc.lab.teamkunpluginmanager.installer.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.installer.task.TaskFailedException;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.garbage.clean.GarbageCleanArgument;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.garbage.clean.GarbageCleanResult;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.garbage.clean.GarbageCleanTask;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.garbage.search.GarbageSearchArgument;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.garbage.search.GarbageSearchTask;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandleManager;
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
 * プラグインの不要データ削除するインストーラーの実装です。
 * 不要データ削除は主に以下の流れで動作します。
 *
 * <ol>
 *     <li>{@link CleanTasks#SEARCHING_GARBAGE} - 不要データを検索する。</li>
 *     <li>{@link CleanTasks#DELETING_GARBAGE} - 不要データを削除する。</li>
 * </ol>
 */
public class GarbageCleaner extends AbstractInstaller<CleanArgument, CleanErrorCause, CleanTasks>
{
    private static final Path PLUGIN_DIR;

    static
    {
        PLUGIN_DIR = KPMDaemon.getInstance().getDataFolderPath().getParent();
    }

    public GarbageCleaner(SignalHandleManager signalHandler) throws IOException
    {
        super(signalHandler);
    }

    @Override
    public InstallResult<CleanTasks> execute(@NotNull CleanArgument argument) throws TaskFailedException
    {
        List<String> excludeDataNames = argument.getExcludeDataNames();
        excludeDataNames.addAll(KPMDaemon.getInstance().getPlugin().getConfig().getStringList("ignore")); // TODO: fix on next commit
        List<String> plugins = Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                .map(Plugin::getName)
                .collect(Collectors.toList());

        GarbageCleanResult result = (GarbageCleanResult) this.submitter(
                        CleanTasks.SEARCHING_GARBAGE,
                        new GarbageSearchTask(this.progress, this.signalHandler)
                )
                .then(
                        CleanTasks.DELETING_GARBAGE,
                        new GarbageCleanTask(this.progress, this.signalHandler)
                )
                .bridgeArgument(searchResult ->
                        new GarbageCleanArgument(searchResult.getGarbageFiles()))
                .submitAll(new GarbageSearchArgument(excludeDataNames, PLUGIN_DIR, plugins));

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
