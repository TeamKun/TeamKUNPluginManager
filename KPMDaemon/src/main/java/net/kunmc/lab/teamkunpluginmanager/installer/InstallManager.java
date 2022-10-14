package net.kunmc.lab.teamkunpluginmanager.installer;

import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.KPMDaemon;
import net.kunmc.lab.teamkunpluginmanager.TokenStore;
import net.kunmc.lab.teamkunpluginmanager.installer.exceptions.InstallerRunningException;
import net.kunmc.lab.teamkunpluginmanager.installer.exceptions.TokenNotAvailableException;
import org.jetbrains.annotations.NotNull;

/**
 * インストールを管理するクラスです。
 */
public class InstallManager
{
    private final KPMDaemon daemon;
    private final TokenStore tokenStore;

    private InstallProgress<?, ?> runningInstall;

    public InstallManager(@NotNull KPMDaemon daemon)
    {
        this.daemon = daemon;
        this.tokenStore = daemon.getTokenStore();

        this.runningInstall = null;
    }

    /**
     * インストールが進行中かどうかを返します。
     *
     * @return インストールが進行中かどうか
     */
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
    @SuppressWarnings("unchecked")
    public <A extends AbstractInstallerArgument, T extends Enum<T>, I extends AbstractInstaller<A, ?, T>> InstallProgress<T, I> runInstallerAsync(
            @NotNull I installer,
            @NotNull A arguments
    )
    {
        if (isRunning())
            throw new InstallerRunningException("Install is already running.");
        if (!this.tokenStore.isTokenAvailable())
            throw new TokenNotAvailableException("Token is not available.");

        this.runningInstall = installer.getProgress();

        Runner.runAsync(() -> installer.run(arguments));

        return (InstallProgress<T, I>) installer.getProgress();
    }
}
