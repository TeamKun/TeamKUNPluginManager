package org.kunlab.kpm.installer;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.TokenStore;
import org.kunlab.kpm.installer.exceptions.InstallerRunningException;
import org.kunlab.kpm.installer.exceptions.TokenNotAvailableException;
import org.kunlab.kpm.installer.interfaces.InstallManager;
import org.kunlab.kpm.installer.interfaces.InstallProgress;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.installer.interfaces.PluginInstaller;

import java.util.function.Consumer;

public class InstallManagerImpl implements InstallManager
{
    private final TokenStore tokenStore;

    private InstallProgress<? extends Enum<?>, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>>> runningInstall;

    public InstallManagerImpl(@NotNull TokenStore store)
    {
        this.tokenStore = store;

        this.runningInstall = null;
    }

    @Override
    public boolean isRunning()
    {
        if (this.runningInstall == null)
            return false;

        if (this.runningInstall.isFinished())
        {
            this.runningInstall = null;
            return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends InstallerArgument, T extends Enum<T>, I extends PluginInstaller<A, ?, T>> InstallProgress<T, I> runInstallerAsync(
            @NotNull I installer,
            @NotNull A arguments,
            @Nullable Consumer<InstallResult<T>> onFinished
    )
    {
        if (this.isRunning())
            throw new InstallerRunningException("Install is already running.");
        if (!this.tokenStore.isTokenAvailable())
            throw new TokenNotAvailableException("Token is not available.");

        this.runningInstall = installer.getProgress();

        Runner.runAsync(() -> {
            InstallResult<T> result = installer.run(arguments);
            if (onFinished != null)
                onFinished.accept(result);
        });

        return (InstallProgress<T, I>) installer.getProgress();
    }
}
