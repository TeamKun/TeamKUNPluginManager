package net.kunmc.lab.kpm.installer.impls.update;

import net.kunmc.lab.kpm.installer.AbstractInstaller;
import net.kunmc.lab.kpm.installer.impls.update.signals.UpdateFinishedSignal;
import net.kunmc.lab.kpm.interfaces.KPMRegistry;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.task.TaskFailedException;
import net.kunmc.lab.kpm.task.tasks.alias.source.download.SourceDownloadArgument;
import net.kunmc.lab.kpm.task.tasks.alias.source.download.SourceDownloadTask;
import net.kunmc.lab.kpm.task.tasks.alias.update.UpdateAliasesArgument;
import net.kunmc.lab.kpm.task.tasks.alias.update.UpdateAliasesResult;
import net.kunmc.lab.kpm.task.tasks.alias.update.UpdateAliasesTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

/**
 * プラグインをエイリアスのアップデートするインストーラーの実装です。
 * エイリアスのアップデートは主に以下の流れで動作します。
 *
 * <ol>
 *     <li>{@link UpdateTasks#DOWNLOADING_SOURCES} - ソースファイルをダウンロードする。</li>
 *     <li>{@link UpdateTasks#UPDATING_ALIASES} - エイリアスのアップデートを行う。</li>
 * </ol>
 */
public class AliasUpdater extends AbstractInstaller<UpdateArgument, UpdateErrorCause, UpdateTasks>
{
    public AliasUpdater(@NotNull KPMRegistry registry, @NotNull SignalHandleManager signalHandler) throws IOException
    {
        super(registry, signalHandler);
        this.progress.setCurrentTask(UpdateTasks.INITIALIZED);
    }

    @Override
    public InstallResult<UpdateTasks> execute(@NotNull UpdateArgument argument) throws TaskFailedException
    {
        Map<String, String> remotes = argument.getRemotes();

        UpdateAliasesResult result =
                this.submitter(
                                UpdateTasks.DOWNLOADING_SOURCES,
                                new SourceDownloadTask(this)
                        )
                        .then(
                                UpdateTasks.UPDATING_ALIASES,
                                new UpdateAliasesTask(this)
                        )
                        .bridgeArgument(sourceDownloadResult ->
                                new UpdateAliasesArgument(sourceDownloadResult.getDownloadedSources()))
                        .submitAll(new SourceDownloadArgument(remotes));

        this.postSignal(new UpdateFinishedSignal(result.getAliasesCount(), result.getAliasesCountBySource()));

        return this.success();
    }
}
