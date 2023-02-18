package org.kunlab.kpm.signal.handlers.kpmupgrade;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.DebugConstants;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.SignalHandlingUtils;
import org.kunlab.kpm.upgrader.signals.AlreadyUpgradingSignal;
import org.kunlab.kpm.upgrader.signals.KPMUpgradeReadySignal;
import org.kunlab.kpm.upgrader.signals.LatestFetchSignal;
import org.kunlab.kpm.upgrader.signals.UpgraderDeploySignal;
import org.kunlab.kpm.versioning.Version;

public class KPMUpgradeSignalHandler
{
    private final Terminal terminal;

    public KPMUpgradeSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onAlreadyUpgradeRunning(AlreadyUpgradingSignal signal)
    {
        this.terminal.error(LangProvider.get("kpmupgrade.upgrade.alreadyRunning"));
    }

    @SignalHandler
    public void onLatestFetchPre(LatestFetchSignal.Pre signal)
    {
        this.terminal.info(LangProvider.get("kpmupgrade.upgrade.fetch.fetching"));
    }

    @SignalHandler
    public void onLatestFetchPost(LatestFetchSignal.Post signal)
    {
        if (DebugConstants.ALLOW_UNNEEDED_UPGRADE)
        {
            this.terminal.info("KPM has detected that the environment is debug mode.");
            if (!signal.isUpgradable())
                this.terminal.info("It doesn't need to upgrade, but it will be upgraded because the environment is debug mode.");
            return;
        }

        if (!signal.isUpgradable())
            this.terminal.info(LangProvider.get("kpmupgrade.upgrade.fetch.alreadyLatest"));
        else if (signal.isFetchedOlderVersion())
        {
            this.terminal.error(LangProvider.get("kpmupgrade.upgrade.fetch.serverOld"));
            this.terminal.hint(LangProvider.get("kpmupgrade.upgrade.fetch.serverOld.hint"));
        }
        else
            this.terminal.info(LangProvider.get("kpmupgrade.upgrade.fetch.upgradable"));
    }

    @SignalHandler
    public void onLatestFetchError(LatestFetchSignal.Error signal)
    {
        this.terminal.error(LangProvider.get("kpmupgrade.upgrade.fetch.fail"));
        this.terminal.hint(LangProvider.get("kpmupgrade.upgrade.fetch.fail.suggest"));
    }

    @SignalHandler
    public void onUpgraderDeployPre(UpgraderDeploySignal.Pre signal)
    {
        this.terminal.info(LangProvider.get("kpmupgrade.upgrade.deploy.deploying"));
    }

    @SignalHandler
    public void onUpgraderDeployPost(UpgraderDeploySignal.Post signal)
    {
        this.terminal.info("KPM アップグレーダを正常に配置しました。");
    }

    @SignalHandler
    public void onUpgraderDeployError(UpgraderDeploySignal.Error signal)
    {
        this.terminal.error(LangProvider.get(
                "kpmupgrade.upgrade.deploy.fail",
                MsgArgs.of("cause", signal.getCause())
        ));
    }

    @SignalHandler
    public void onUpgradeReady(KPMUpgradeReadySignal signal)
    {
        Version from = signal.getCurrentKPMVersion();
        Version to = signal.getToKPMVersion();

        this.terminal.info(LangProvider.get(
                "kpmupgrade.upgrade.ready",
                MsgArgs.of("from", from)
                        .add("to", to)
        ));

        this.terminal.warn(LangProvider.get("kpmupgrade.upgrade.ready.warn"));

        boolean isContinue = SignalHandlingUtils.askContinue(this.terminal);
        signal.setContinueUpgrade(isContinue);

        if (!isContinue)
            this.terminal.error(LangProvider.get("kpmupgrade.upgrade.ready.cancel"));
    }

}
