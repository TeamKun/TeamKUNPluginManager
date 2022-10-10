package net.kunmc.lab.teamkunpluginmanager;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.installer.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.installer.AbstractInstallerArgument;
import net.kunmc.lab.teamkunpluginmanager.installer.exceptions.InstallerRunningException;
import net.kunmc.lab.teamkunpluginmanager.installer.exceptions.TokenNotAvailableException;
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
import net.kunmc.lab.teamkunpluginmanager.signal.HeadSignalHandlers;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandleManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
public class HeadInstallers
{
    private final KPMDaemon daemon;

    private <A extends AbstractInstallerArgument, T extends Enum<T>, I extends AbstractInstaller<A, ?, T>> void headRun(
            @NotNull Terminal terminal,
            @NotNull I installer,
            @NotNull A arguments
    )
    {
        try
        {
            this.daemon.getInstallManager().runInstallerAsync(installer, arguments);
        }
        catch (InstallerRunningException e)
        {
            terminal.error("他のインストールが実行中です。");
        }
        catch (TokenNotAvailableException e)
        {
            terminal.error("トークンが設定されていません！");
            terminal.info("/kpm register でトークンを設定してください。");
        }
    }

    /**
     * インストールを実行します。
     *
     * @param terminal ターミナル
     * @param argument インストールの引数
     */
    public void runInstall(@NotNull Terminal terminal, @NotNull InstallArgument argument)
    {
        SignalHandleManager handleManager = new SignalHandleManager();
        HeadSignalHandlers.getInstallHandlers(terminal).forEach(handleManager::register);

        PluginInstaller installer;
        try
        {
            installer = new PluginInstaller(this.daemon, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：" + e.getMessage());
            return;
        }

        this.headRun(terminal, installer, argument);
    }

    /**
     * アンインストールを実行します。
     *
     * @param terminal ターミナル
     * @param argument アンインストールするプラグインのクエリ
     */
    public void runUninstall(@NotNull Terminal terminal, @NotNull UninstallArgument argument)
    {
        SignalHandleManager handleManager = new SignalHandleManager();
        HeadSignalHandlers.getUninstallHandlers(terminal).forEach(handleManager::register);

        PluginUninstaller uninstaller;
        try
        {
            uninstaller = new PluginUninstaller(this.daemon, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：" + e.getMessage());
            return;
        }

        this.headRun(terminal, uninstaller, argument);
    }

    public void runUpdate(@NotNull Terminal terminal, @NotNull UpdateArgument argument)
    {
        SignalHandleManager handleManager = new SignalHandleManager();
        HeadSignalHandlers.getUpdateHandlers(terminal).forEach(handleManager::register);

        AliasUpdater updater;
        try
        {
            updater = new AliasUpdater(this.daemon, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：" + e.getMessage());
            return;
        }

        this.headRun(terminal, updater, argument);
    }

    /**
     * プラグインの自動削除を実行します。
     *
     * @param terminal ターミナル
     * @param argument 自動削除の引数
     */
    public void runAutoRemove(@NotNull Terminal terminal, @NotNull AutoRemoveArgument argument)
    {
        SignalHandleManager handleManager = new SignalHandleManager();
        HeadSignalHandlers.getAutoRemoveHandlers(terminal).forEach(handleManager::register);

        PluginAutoRemover autoRemover;
        try
        {
            autoRemover = new PluginAutoRemover(this.daemon, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：" + e.getMessage());
            return;
        }

        this.headRun(terminal, autoRemover, argument);
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
        SignalHandleManager handleManager = new SignalHandleManager();
        HeadSignalHandlers.getGarbageCleanHandlers(terminal).forEach(handleManager::register);

        GarbageCleaner garbageCleaner;
        try
        {
            garbageCleaner = new GarbageCleaner(handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：" + e.getMessage());
            return;
        }

        this.headRun(terminal, garbageCleaner, argument);
    }
}
