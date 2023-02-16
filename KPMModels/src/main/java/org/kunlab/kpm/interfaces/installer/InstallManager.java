package org.kunlab.kpm.interfaces.installer;

import org.jetbrains.annotations.NotNull;

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
     * @param installer インストーラ
     * @param arguments インストーラに渡す引数
     * @param <A>       インストーラの引数の型
     * @param <T>       インストールのタスクの型
     * @param <I>       インストーラの型
     * @return インストールの結果
     */
    <A extends InstallerArgument, T extends Enum<T>, I extends PluginInstaller<A, ?, T>> InstallProgress<T, I> runInstallerAsync(
            @NotNull I installer,
            @NotNull A arguments
    );
}
