package net.kunmc.lab.kpm;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.installer.AbstractInstaller;
import net.kunmc.lab.kpm.installer.exceptions.InstallerRunningException;
import net.kunmc.lab.kpm.installer.exceptions.TokenNotAvailableException;
import net.kunmc.lab.kpm.installer.impls.autoremove.AutoRemoveArgument;
import net.kunmc.lab.kpm.installer.impls.autoremove.PluginAutoRemover;
import net.kunmc.lab.kpm.installer.impls.clean.CleanArgument;
import net.kunmc.lab.kpm.installer.impls.clean.GarbageCleaner;
import net.kunmc.lab.kpm.installer.impls.install.InstallArgument;
import net.kunmc.lab.kpm.installer.impls.install.PluginInstaller;
import net.kunmc.lab.kpm.installer.impls.register.RegisterArgument;
import net.kunmc.lab.kpm.installer.impls.register.TokenRegisterer;
import net.kunmc.lab.kpm.installer.impls.uninstall.PluginUninstaller;
import net.kunmc.lab.kpm.installer.impls.uninstall.UninstallArgument;
import net.kunmc.lab.kpm.installer.impls.update.AliasUpdater;
import net.kunmc.lab.kpm.installer.impls.update.UpdateArgument;
import net.kunmc.lab.kpm.installer.impls.upgrade.PluginUpgrader;
import net.kunmc.lab.kpm.installer.impls.upgrade.UpgradeArgument;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.signal.HeadSignalHandlers;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
public class HeadInstallers
{
    private final KPMRegistry registry;

    private <A extends InstallerArgument, T extends Enum<T>, I extends AbstractInstaller<A, ?, T>> void headRun(
            @NotNull Terminal terminal,
            @NotNull I installer,
            @NotNull A arguments
    )
    {
        try
        {
            this.registry.getInstallManager().runInstallerAsync(installer, arguments);
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

    public void runInstall(@NotNull Terminal terminal, @NotNull InstallArgument argument)
    {
        SignalHandleManager handleManager = new SignalHandleManager();
        HeadSignalHandlers.getInstallHandlers(terminal).forEach(handleManager::register);

        PluginInstaller installer;
        try
        {
            installer = new PluginInstaller(this.registry, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：%s", e.getMessage());
            return;
        }

        this.headRun(terminal, installer, argument);
    }

    public void runUninstall(@NotNull Terminal terminal, @NotNull UninstallArgument argument)
    {
        SignalHandleManager handleManager = new SignalHandleManager();
        HeadSignalHandlers.getUninstallHandlers(terminal).forEach(handleManager::register);

        PluginUninstaller uninstaller;
        try
        {
            uninstaller = new PluginUninstaller(this.registry, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：%s", e.getMessage());
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
            updater = new AliasUpdater(this.registry, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：%s", e.getMessage());
            return;
        }

        this.headRun(terminal, updater, argument);
    }

    public void runAutoRemove(@NotNull Terminal terminal, @NotNull AutoRemoveArgument argument)
    {
        SignalHandleManager handleManager = new SignalHandleManager();
        HeadSignalHandlers.getAutoRemoveHandlers(terminal).forEach(handleManager::register);

        PluginAutoRemover autoRemover;
        try
        {
            autoRemover = new PluginAutoRemover(this.registry, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：%s", e.getMessage());
            return;
        }

        this.headRun(terminal, autoRemover, argument);
    }

    public void runGarbageClean(@NotNull Terminal terminal, @NotNull CleanArgument argument) throws IllegalStateException
    {
        SignalHandleManager handleManager = new SignalHandleManager();
        HeadSignalHandlers.getGarbageCleanHandlers(terminal).forEach(handleManager::register);

        GarbageCleaner garbageCleaner;
        try
        {
            garbageCleaner = new GarbageCleaner(this.registry, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：%s", e.getMessage());
            return;
        }

        this.headRun(terminal, garbageCleaner, argument);
    }

    public void runRegister(@NotNull Terminal terminal, @NotNull RegisterArgument argument) throws IllegalStateException
    {
        SignalHandleManager handleManager = new SignalHandleManager();
        HeadSignalHandlers.getTokenRegistererHandlers(terminal).forEach(handleManager::register);

        TokenRegisterer register;
        try
        {
            register = new TokenRegisterer(this.registry, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：%s", e.getMessage());
            return;
        }

        this.headRun(terminal, register, argument);
    }

    public void runUpgrade(@NotNull Terminal terminal, @NotNull UpgradeArgument argument) throws IllegalStateException
    {
        SignalHandleManager handleManager = new SignalHandleManager();
        boolean isAuto = argument.getTargetPlugins() == null || argument.getTargetPlugins().isEmpty();

        HeadSignalHandlers.getUpgraderHandlers(terminal, isAuto)
                .forEach(handleManager::register);

        PluginUpgrader upgrader;
        try
        {
            upgrader = new PluginUpgrader(this.registry, handleManager);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            terminal.error("インストーラの初期化に失敗しました：%s", e.getMessage());
            return;
        }

        this.headRun(terminal, upgrader, argument);
    }
}
