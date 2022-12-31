package net.kunmc.lab.kpm.installer;

import net.kunmc.lab.kpm.TokenStore;
import net.kunmc.lab.kpm.installer.exceptions.InstallerRunningException;
import net.kunmc.lab.kpm.installer.exceptions.TokenNotAvailableException;
import net.kunmc.lab.kpm.interfaces.installer.InstallManager;
import net.kunmc.lab.kpm.interfaces.installer.InstallProgress;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.jetbrains.annotations.NotNull;

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
            @NotNull A arguments
    )
    {
        if (this.isRunning())
            throw new InstallerRunningException("Install is already running.");
        if (!this.tokenStore.isTokenAvailable())
            throw new TokenNotAvailableException("Token is not available.");

        this.runningInstall = installer.getProgress();

        Runner.runAsync(() -> installer.run(arguments));

        return (InstallProgress<T, I>) installer.getProgress();
    }
}
