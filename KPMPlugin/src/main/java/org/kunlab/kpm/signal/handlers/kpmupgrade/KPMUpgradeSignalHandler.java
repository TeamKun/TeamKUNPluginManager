package org.kunlab.kpm.signal.handlers.kpmupgrade;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.kunlab.kpm.DebugConstants;
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
        this.terminal.error("アップグレートは既に進行中です。");
    }

    @SignalHandler
    public void onLatestFetchPre(LatestFetchSignal.Pre signal)
    {
        this.terminal.info("KPM の最新バージョンを確認しています …");
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
            this.terminal.info("すばらしい！ 現在の KPM は最新バージョンです。アップグレードの必要はありません。");
        else if (signal.isFetchedOlderVersion())
        {
            this.terminal.error("KPM の最新バージョンは、現在使用している KPM のバージョンよりも古いです。");
            this.terminal.hint("KPM の開発環境で実行している可能性があります。");
        }
        else
            this.terminal.info("KPM はアップグレード可能です。");
    }

    @SignalHandler
    public void onLatestFetchError(LatestFetchSignal.Error signal)
    {
        this.terminal.error("KPM の最新バージョンの確認に失敗しました。");
        this.terminal.hint("しばらく時間をおいてから再度実行してください。");
    }

    @SignalHandler
    public void onUpgraderDeployPre(UpgraderDeploySignal.Pre signal)
    {
        this.terminal.info("KPM アップグレーダを配置しています …");
    }

    @SignalHandler
    public void onUpgraderDeployPost(UpgraderDeploySignal.Post signal)
    {
        this.terminal.info("KPM アップグレーダを正常に配置しました。");
    }

    @SignalHandler
    public void onUpgraderDeployError(UpgraderDeploySignal.Error signal)
    {
        this.terminal.error("KPM アップグレーダの配置に失敗しました：%s", signal.getCause());
        if (signal.getCause() == UpgraderDeploySignal.Error.ErrorCause.DEPLOYER_NOT_EXISTS)
            this.terminal.info("この KPM のビルドは、 KPM を含んでいない可能性があります。");
    }

    @SignalHandler
    public void onUpgradeReady(KPMUpgradeReadySignal signal)
    {
        Version from = signal.getCurrentKPMVersion();
        Version to = signal.getToKPMVersion();

        this.terminal.info("TeamKUNPluginManager(%s) は、 %s にアップグレードします。", from, to);

        this.terminal.warn(ChatColor.DARK_RED + "KPM アップグレーダは、アップグレード完了後にサーバをリロードします。");

        boolean isContinue = SignalHandlingUtils.askContinue(this.terminal);
        signal.setContinueUpgrade(isContinue);

        if (!isContinue)
            this.terminal.error("アップグレードがキャンセルされました。");
    }

}