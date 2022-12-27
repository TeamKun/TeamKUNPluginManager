package net.kunmc.lab.kpm.upgrader;

import lombok.Getter;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.TeamKunPluginManager;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.upgrader.signals.KPMUpgradeReadySignal;
import net.kunmc.lab.kpm.upgrader.signals.LatestFetchSignal;
import net.kunmc.lab.kpm.upgrader.signals.UpgraderDeploySignal;
import net.kunmc.lab.kpm.utils.PluginUtil;
import net.kunmc.lab.kpm.utils.versioning.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class KPMUpgrader
{
    private static final String UPGRADER_JAR_NAME = "KPMUpgrader-%version%.jar";
    private static final Path upgraderPath = Paths.get("XX.KPMUpgrader.jar");

    private final TeamKunPluginManager teamKUNPluginManager;
    private final KPMDaemon daemon;
    private final Version currentKPMVersion;

    private Version toKPMVersion;

    @Getter
    private volatile boolean upgradeRunning;

    public KPMUpgrader(TeamKunPluginManager teamKUNPluginManager, KPMDaemon daemon)
    {
        this.teamKUNPluginManager = teamKUNPluginManager;
        this.daemon = daemon;

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

        Path upgraderJar = this.teamKUNPluginManager.getDataFolder().toPath().resolve(upgraderPath);

        if (!this.deployUpgrader(signalHandleManager, upgraderJar))
            return;

        this.daemon.getPluginLoader().loadPlugin(upgraderJar);
    }

    private boolean checkCanUpgrade(SignalHandleManager signalHandleManager)
    {
        signalHandleManager.handleSignal(new LatestFetchSignal.Pre(this.currentKPMVersion));

        LatestFetchSignal.Post fetchedSignal;
        try
        {
            this.toKPMVersion = KPMFetcher.fetchLatestKPMVersion(this.daemon);

            fetchedSignal = new LatestFetchSignal.Post(this.currentKPMVersion, this.toKPMVersion);
            signalHandleManager.handleSignal(fetchedSignal);
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();

            signalHandleManager.handleSignal(new LatestFetchSignal.Error(this.currentKPMVersion, e.getMessage()));
            return false;
        }

        return fetchedSignal.isUpgradable();
    }

    private boolean deployUpgrader(SignalHandleManager signalHandleManager, Path to)
    {
        String jarName = UPGRADER_JAR_NAME.replace(
                "%version%",
                this.teamKUNPluginManager.getDescription().getVersion()
        );

        if (Files.exists(to))
        {
            signalHandleManager.handleSignal(
                    new UpgraderDeploySignal.Error(UpgraderDeploySignal.Error.ErrorCause.ALREADY_DEPLOYED)
            );

            return false;
        }

        signalHandleManager.handleSignal(new UpgraderDeploySignal.Pre(to));

        File kpmJar = PluginUtil.getFile(this.teamKUNPluginManager);

        try (ZipFile zipFile = new ZipFile(kpmJar))
        {
            ZipEntry entry = zipFile.getEntry(jarName);
            if (entry == null)
            {
                signalHandleManager.handleSignal(
                        new UpgraderDeploySignal.Error(UpgraderDeploySignal.Error.ErrorCause.DEPLOYER_NOT_EXISTS)
                );
                return false;
            }

            try (InputStream in = zipFile.getInputStream(entry))
            {
                Files.copy(in, to);
            }

            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            signalHandleManager.handleSignal(
                    new UpgraderDeploySignal.Error(UpgraderDeploySignal.Error.ErrorCause.IO_EXCEPTION_OCCURRED)
            );

            return false;
        }
    }

}
