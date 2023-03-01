package org.kunlab.kpm;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.AbstractInstaller;
import org.kunlab.kpm.installer.exceptions.InstallerRunningException;
import org.kunlab.kpm.installer.exceptions.TokenNotAvailableException;
import org.kunlab.kpm.installer.impls.autoremove.AutoRemoveArgument;
import org.kunlab.kpm.installer.impls.autoremove.PluginAutoRemover;
import org.kunlab.kpm.installer.impls.clean.CleanArgument;
import org.kunlab.kpm.installer.impls.clean.GarbageCleaner;
import org.kunlab.kpm.installer.impls.install.InstallArgument;
import org.kunlab.kpm.installer.impls.install.PluginInstaller;
import org.kunlab.kpm.installer.impls.register.RegisterArgument;
import org.kunlab.kpm.installer.impls.register.TokenRegisterer;
import org.kunlab.kpm.installer.impls.uninstall.PluginUninstaller;
import org.kunlab.kpm.installer.impls.uninstall.UninstallArgument;
import org.kunlab.kpm.installer.impls.update.AliasUpdater;
import org.kunlab.kpm.installer.impls.update.UpdateArgument;
import org.kunlab.kpm.installer.impls.upgrade.PluginUpgrader;
import org.kunlab.kpm.installer.impls.upgrade.UpgradeArgument;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.HeadSignalHandlers;
import org.kunlab.kpm.signal.SignalHandleManager;

import java.io.IOException;
import java.util.function.Consumer;

@AllArgsConstructor
public class HeadInstallers
{
    private final KPMRegistry registry;
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
            terminal.error(LangProvider.get("installer.init_failed", MsgArgs.of("error", e.getMessage())));
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
            terminal.error(LangProvider.get("installer.init_failed", MsgArgs.of("error", e.getMessage())));
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
            terminal.error(LangProvider.get("installer.init_failed", MsgArgs.of("error", e.getMessage())));
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
            terminal.error(LangProvider.get("installer.init_failed", MsgArgs.of("error", e.getMessage())));
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
            terminal.error(LangProvider.get("installer.init_failed", MsgArgs.of("error", e.getMessage())));
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
            terminal.error(LangProvider.get("installer.init_failed", MsgArgs.of("error", e.getMessage())));
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
            terminal.error(LangProvider.get("installer.init_failed", MsgArgs.of("error", e.getMessage())));
            return;
        }

        this.headRun(terminal, upgrader, argument);
    }

    private <A extends InstallerArgument, T extends Enum<T>, I extends AbstractInstaller<A, ?, T>> void headRun(
            @NotNull Terminal terminal,
            @NotNull I installer,
            @NotNull A arguments
    )
    {
        try
        {
            this.registry.getInstallManager().runInstallerAsync(
                    installer,
                    arguments,
                    new InstallCallbackRecipient<>(this.registry, terminal)
            );
        }
        catch (InstallerRunningException e)
        {
            terminal.error(LangProvider.get("installer.another_running"));
        }
        catch (TokenNotAvailableException e)
        {
            Notices.printTokenUnset(this.registry, terminal);
        }
    }

    @AllArgsConstructor
    private static class InstallCallbackRecipient<T extends Enum<T>> implements Consumer<InstallResult<T>>
    {
        private final KPMRegistry registry;
        private final Terminal terminal;

        @Override
        public void accept(InstallResult<T> ignored)
        {
            Notices.printAllNotice(this.registry, this.terminal);
        }
    }
}
