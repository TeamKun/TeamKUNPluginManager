package org.kunlab.kpm.installer.impls.update;

import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.AbstractInstaller;
import org.kunlab.kpm.installer.impls.update.signals.UpdateFinishedSignal;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.interfaces.installer.InstallResult;
import org.kunlab.kpm.signal.SignalHandleManager;
import org.kunlab.kpm.task.TaskFailedException;
import org.kunlab.kpm.task.tasks.alias.source.download.SourceDownloadArgument;
import org.kunlab.kpm.task.tasks.alias.source.download.SourceDownloadTask;
import org.kunlab.kpm.task.tasks.alias.update.UpdateAliasesArgument;
import org.kunlab.kpm.task.tasks.alias.update.UpdateAliasesResult;
import org.kunlab.kpm.task.tasks.alias.update.UpdateAliasesTask;

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
