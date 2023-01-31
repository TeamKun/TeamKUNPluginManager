package net.kunmc.lab.kpm.upgrader;

import lombok.Getter;
import net.kunmc.lab.kpm.DebugConstants;
import net.kunmc.lab.kpm.TeamKunPluginManager;
import net.kunmc.lab.kpm.http.RequestMethod;
import net.kunmc.lab.kpm.http.Requests;
import net.kunmc.lab.kpm.interfaces.KPMRegistry;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.upgrader.signals.KPMUpgradeReadySignal;
import net.kunmc.lab.kpm.upgrader.signals.LatestFetchSignal;
import net.kunmc.lab.kpm.upgrader.signals.UpgraderDeploySignal;
import net.kunmc.lab.kpm.versioning.Version;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KPMUpgrader
{
    private static final Path upgraderPath = Paths.get("XX.KPMUpgrader.jar");

    private final TeamKunPluginManager teamKUNPluginManager;
    private final KPMRegistry registry;
    private final Version currentKPMVersion;

    private Version toKPMVersion;

    @Getter
    private volatile boolean upgradeRunning;

    public KPMUpgrader(TeamKunPluginManager teamKUNPluginManager, KPMRegistry registry)
    {
        this.teamKUNPluginManager = teamKUNPluginManager;
        this.registry = registry;

        this.currentKPMVersion = Version.of(teamKUNPluginManager.getDescription().getVersion());
        this.upgradeRunning = false;
        this.toKPMVersion = null;
    }

    public void runUpgrade(SignalHandleManager signalHandleManager)
    {
        if (!this.checkCanUpgrade(signalHandleManager))
            return;

        KPMUpgradeReadySignal readySignal = new KPMUpgradeReadySignal(this.currentKPMVersion, this.toKPMVersion);
        signalHandleManager.handleSignal(readySignal);
        if (!readySignal.isContinueUpgrade())
            return;

        this.upgradeRunning = true;

        Path cachesDir = this.teamKUNPluginManager.getDataFolder().toPath().resolve(".caches");
        Path upgradeCacheDir;
        try
        {
            upgradeCacheDir = Files.createTempDirectory(cachesDir, "upgrade-");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        Path upgraderJar = upgradeCacheDir.resolve(upgraderPath);

        if (!this.deployUpgrader(signalHandleManager, upgraderJar))
            return;

        this.teamKUNPluginManager.getDaemon().getPluginMetaManager().preparePluginModify("KPMUpgrader");

        Runner.run(() -> {
            this.registry.getPluginLoader().loadPlugin(upgraderJar);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kpm-upgrade-internal " + this.toKPMVersion.toString());
        });
    }

    private boolean checkCanUpgrade(SignalHandleManager signalHandleManager)
    {
        signalHandleManager.handleSignal(new LatestFetchSignal.Pre(this.currentKPMVersion));

        LatestFetchSignal.Post fetchedSignal;
        try
        {
            this.toKPMVersion = KPMFetcher.fetchLatestKPMVersion(this.registry);

            fetchedSignal = new LatestFetchSignal.Post(this.currentKPMVersion, this.toKPMVersion);
            signalHandleManager.handleSignal(fetchedSignal);
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();

            signalHandleManager.handleSignal(new LatestFetchSignal.Error(this.currentKPMVersion, e.getMessage()));
            return false;
        }

        return fetchedSignal.isUpgradable() || DebugConstants.ALLOW_UNNEEDED_UPGRADE;
    }

    private boolean deployUpgrader(SignalHandleManager signalHandleManager, Path to)
    {
        if (Files.exists(to))
        {
            signalHandleManager.handleSignal(
                    new UpgraderDeploySignal.Error(UpgraderDeploySignal.Error.ErrorCause.ALREADY_DEPLOYED)
            );

            return false;
        }

        signalHandleManager.handleSignal(new UpgraderDeploySignal.Pre(to));

        try
        {
            String downloadFile = KPMFetcher.fetchUpgraderJarFile(this.registry);
            Requests.downloadFile(RequestMethod.GET, downloadFile, to);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();

            signalHandleManager.handleSignal(
                    new UpgraderDeploySignal.Error(UpgraderDeploySignal.Error.ErrorCause.DEPLOYER_NOT_EXISTS)
            );

            return false;
        }
    }

}
