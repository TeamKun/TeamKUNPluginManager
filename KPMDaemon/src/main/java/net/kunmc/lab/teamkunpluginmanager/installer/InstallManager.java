package net.kunmc.lab.teamkunpluginmanager.installer;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.KPMDaemon;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.autoremove.AutoRemoveArgument;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.autoremove.PluginAutoRemover;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.clean.CleanArgument;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.clean.GarbageCleaner;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.install.InstallArgument;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.install.PluginInstaller;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.uninstall.PluginUninstaller;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.uninstall.UninstallArgument;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.update.AliasUpdater;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.update.UpdateArgument;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandleManager;
import net.kunmc.lab.teamkunpluginmanager.utils.TokenStore;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * プラグインのインストールを管理するクラスです。
 */
public class InstallManager
{
    private final KPMDaemon daemon;
    private final TokenStore tokenStore;
    private final SignalHandleManager signalHandleManager;

    private InstallProgress<?, ?> runningInstall;

    public InstallManager(@NotNull KPMDaemon daemon)
    {
        this.daemon = daemon;
        this.tokenStore = daemon.getTokenStore();
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
        if (!this.tokenStore.isTokenAvailable())
        {
            terminal.error("GitHub にログインされていません。");
            terminal.info("/kpm register でログインしてください。");
            return;
        }

        SignalHandleManager copiedHandleManager = signalHandleManager.copy();  // TODO: Fix in next commit
        HeadSignalHandlers.getInstallHandlers(terminal).forEach(copiedHandleManager::register);

        PluginInstaller installer;
        try
        {
            installer = new PluginInstaller(this.daemon, copiedHandleManager);
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
    public void runUninstall(@NotNull Terminal terminal, @NotNull UninstallArgument argument) throws IllegalStateException
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
            PluginUninstaller uninstaller = new PluginUninstaller(this.daemon, copiedHandleManager);
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
            AliasUpdater updater = new AliasUpdater(this.daemon, copiedHandleManager);
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

    /**
     * プラグインの自動削除を実行します。
     *
     * @param terminal ターミナル
     * @param argument 自動削除の引数
     */
    public void runAutoRemove(@NotNull Terminal terminal, @NotNull AutoRemoveArgument argument)
    {
        if (isRunning())
        {
            terminal.error("他のインストーラが起動しています。");
            return;
        }

        SignalHandleManager copiedHandleManager = signalHandleManager.copy();
        HeadSignalHandlers.getAutoRemoveHandlers(terminal).forEach(copiedHandleManager::register);

        try
        {
            PluginAutoRemover remover = new PluginAutoRemover(this.daemon, copiedHandleManager);
            runningInstall = remover.getProgress();

            remover.run(argument);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            terminal.error("不明なエラーが発生しました。");

            runningInstall = null;
        }
    }

    /**
     * 不要データ(ガベージ)の削除を実行します。
     *
     * @param terminal ターミナル
     * @param argument 不要データ削除の引数
     * @throws IllegalStateException インストールが進行中の場合
     */
    public void runGarbageClean(@NotNull Terminal terminal, @NotNull CleanArgument argument) throws IllegalStateException
    {
        if (isRunning())
        {
            terminal.error("他のインストーラが起動しています。");
            return;
        }

        SignalHandleManager copiedHandleManager = signalHandleManager.copy();
        HeadSignalHandlers.getGarbageCleanHandlers(terminal).forEach(copiedHandleManager::register);

        try
        {
            GarbageCleaner collector = new GarbageCleaner(copiedHandleManager);
            runningInstall = collector.getProgress();

            collector.run(argument);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            terminal.error("不明なエラーが発生しました。");

            runningInstall = null;
        }
    }
}
