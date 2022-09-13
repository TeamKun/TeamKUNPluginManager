package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * インストールに失敗したことを表すインストール結果です。
 *
 * @param <P> インストールの進捗状況の型
 * @param <T> 失敗した理由の型
 * @param <S> タスクの状態の型
 */
@Getter
public class InstallFailedInstallResult<P extends Enum<P>, T extends Enum<T>, S extends Enum<S>>
        extends InstallResult<P>
{
    @Nullable
    private final T reason;
    @Nullable
    private final S taskStatus;

    public InstallFailedInstallResult(@NotNull InstallProgress<P, ?> progress, @Nullable T reason, @NotNull S taskStatus)
    {
        super(false, progress);
        this.reason = reason;
        this.taskStatus = taskStatus;
    }

    public InstallFailedInstallResult(InstallProgress<P, ?> progress, @Nullable T reason)
    {
        super(false, progress);
        this.reason = reason;
        this.taskStatus = null;
    }
}
