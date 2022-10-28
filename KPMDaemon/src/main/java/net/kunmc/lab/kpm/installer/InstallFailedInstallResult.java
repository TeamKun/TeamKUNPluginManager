package net.kunmc.lab.kpm.installer;

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
    /**
     * 例外が発生した場合の例外
     */
    @Nullable
    private final Exception exception;

    /**
     * 失敗した理由を表す列挙型です。
     */
    @Nullable
    private final T reason;
    /**
     * タスクの状態を表す列挙型です。
     */
    @Nullable
    private final S taskStatus;

    public InstallFailedInstallResult(@NotNull InstallProgress<P, ?> progress, @Nullable T reason, @NotNull S taskStatus)
    {
        super(false, progress);
        this.exception = null;
        this.reason = reason;
        this.taskStatus = taskStatus;
    }

    public InstallFailedInstallResult(InstallProgress<P, ?> progress, @Nullable T reason)
    {
        super(false, progress);
        this.exception = null;
        this.reason = reason;
        this.taskStatus = null;
    }

    public InstallFailedInstallResult(@NotNull InstallProgress<P, ?> progress, @NotNull Exception exception)
    {
        super(false, progress);
        this.exception = exception;
        this.reason = null;
        this.taskStatus = null;
    }
}
