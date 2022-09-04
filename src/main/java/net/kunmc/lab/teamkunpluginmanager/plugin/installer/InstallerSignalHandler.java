package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import org.jetbrains.annotations.NotNull;

/**
 * {@link InstallerSignal} を受け取るハンドラーです。
 */
@FunctionalInterface
public interface InstallerSignalHandler
{
    /**
     * シグナルを受け取ります。
     *
     * @param installProgress シグナルを受け取ったインストールの進捗状況
     * @param signal          受け取ったシグナル
     * @param <T>             シグナルの型
     */
    <T extends InstallerSignal> void handleSignal(@NotNull InstallProgress<?> installProgress, @NotNull T signal);
}
