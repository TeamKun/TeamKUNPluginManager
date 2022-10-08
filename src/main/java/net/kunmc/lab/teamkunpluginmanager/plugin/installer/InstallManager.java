package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.commands.signal.HeadSignalHandlers;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.install.InstallArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.install.PluginInstaller;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.PluginUninstaller;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.UninstallArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.update.AliasUpdater;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.update.UpdateArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandleManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * プラグインのインストールを管理するクラスです。
 */
public class InstallManager
{
    private final TeamKunPluginManager pluginManager;
    private final SignalHandleManager signalHandleManager;

    private InstallProgress<?, ?> runningInstall;

    public InstallManager(@NotNull TeamKunPluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
        this.signalHandleManager = new SignalHandleManager();

        this.runningInstall = null;
    }

    /**
     * インストールが進行中かどうかを返します。
     *
     * @return インストールが進行中かどうか
     */
    public boolean isRunning()
    {
        if (runningInstall == null)
            return false;

        if (runningInstall.isFinished())
        {
            runningInstall = null;
            return false;
        }

        return true;
    }

    /**
     * インストールを実行します。
     *
     * @param terminal ターミナル
     * @param argument インストールの引数
     */
    public void runInstall(@NotNull Terminal terminal, @NotNull InstallArgument argument)
    {
        if (isRunning())
        {
            terminal.error("他のインストーラが起動しています。");
            return;
        }
        if (!this.pluginManager.isTokenAvailable())
        {
            terminal.error("GitHub にログインされていません。");
            terminal.info("/kpm register でログインしてください。");
            return;
        }

        SignalHandleManager copiedHandleManager = signalHandleManager.copy();
        HeadSignalHandlers.getInstallHandlers(terminal).forEach(copiedHandleManager::register);

        PluginInstaller installer;
        try
        {
            installer = new PluginInstaller(copiedHandleManager);
            runningInstall = installer.getProgress();

            installer.run(argument);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("不明なエラーが発生しました。");

            runningInstall = null;
        }
    }

    /**
     * アンインストールを実行します。
     *
     * @param terminal ターミナル
     * @param argument アンインストールするプラグインのクエリ
     * @throws IllegalStateException インストールが進行中の場合
     * @throws IOException           予期しない例外が発生した場合
     */
    public void runUninstall(@NotNull Terminal terminal, @NotNull UninstallArgument argument) throws IllegalStateException, IOException
    {
        if (isRunning())
        {
            terminal.error("他のインストーラが起動しています。");
            return;
        }

        SignalHandleManager copiedHandleManager = signalHandleManager.copy();
        HeadSignalHandlers.getUninstallHandlers(terminal).forEach(copiedHandleManager::register);

        try
        {
            PluginUninstaller uninstaller = new PluginUninstaller(copiedHandleManager);
            runningInstall = uninstaller.getProgress();

            uninstaller.run(argument);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            terminal.error("不明なエラーが発生しました。");

            runningInstall = null;
        }
    }

    public void runUpdate(@NotNull Terminal terminal, @NotNull UpdateArgument argument)
    {
        if (isRunning())
        {
            terminal.error("他のインストーラが起動しています。");
            return;
        }

        SignalHandleManager copiedHandleManager = signalHandleManager.copy();
        HeadSignalHandlers.getUpdateHandlers(terminal).forEach(copiedHandleManager::register);

        try
        {
            AliasUpdater updater = new AliasUpdater(copiedHandleManager);
            runningInstall = updater.getProgress();

            updater.run(argument);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            terminal.error("不明なエラーが発生しました。");

            runningInstall = null;
        }
    }
}
