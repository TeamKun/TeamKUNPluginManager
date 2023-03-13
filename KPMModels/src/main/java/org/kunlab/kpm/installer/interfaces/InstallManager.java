package org.kunlab.kpm.installer.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * インストールを管理するクラスです。
 */
public interface InstallManager
{
    /**
     * インストールが進行中かどうかを返します。
     *
     * @return インストールが進行中かどうか
     */
    boolean isRunning();

    /**
     * インストーラを実行します。
     *
     * @param installer  インストーラ
     * @param arguments  インストーラに渡す引数
     * @param onFinished インストールが終了したときに呼び出されるコールバック
     * @param <A>        インストーラの引数の型
     * @param <T>        インストールのタスクの型
     * @param <I>        インストーラの型
     * @return インストールの結果
     */
    <A extends InstallerArgument, T extends Enum<T>, I extends Installer<A, ?, T>> InstallProgress<T, I> runInstallerAsync(
            @NotNull I installer,
            @NotNull A arguments,
            @Nullable Consumer<InstallResult<T>> onFinished
    );
}
