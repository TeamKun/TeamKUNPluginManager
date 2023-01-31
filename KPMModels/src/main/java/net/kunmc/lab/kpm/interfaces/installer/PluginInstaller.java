package net.kunmc.lab.kpm.interfaces.installer;

import net.kunmc.lab.kpm.interfaces.KPMRegistry;

/**
 * インストーラの基底クラスです。
 *
 * @param <A> インストーラの引数の型
 * @param <E> インストールのタスクの列挙型
 * @param <P> インストールのタスクの引数の型
 */
public interface PluginInstaller<A extends InstallerArgument, E extends Enum<E>, P extends Enum<P>>
{
    /**
     * インストーラを実行します。
     *
     * @param arguments インストーラに渡す引数
     * @return インストールの結果
     */
    InstallResult<P> run(A arguments);

    KPMRegistry getRegistry();

    InstallProgress<P, PluginInstaller<A, E, P>> getProgress();
}