package net.kunmc.lab.kpm.signal.handlers.common;

import net.kunmc.lab.kpm.installer.impls.install.signals.AlreadyInstalledPluginSignal;
import net.kunmc.lab.kpm.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.kpm.installer.task.tasks.install.signals.PluginIncompatibleWithKPMSignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.signal.SignalHandlingUtils;
import net.kunmc.lab.kpm.utils.Utils;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * プラグインのインストール環境をチェックするハンドラです.
 */
public class CheckEnvSignalHandler
{
    private final Terminal terminal;

    public CheckEnvSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onIncompatibleWithKPM(PluginIncompatibleWithKPMSignal signal)
    {
        this.terminal.warn(
                "%s はこの TeamKUNPluginManager と互換性がありません。",
                Utils.getPluginString(signal.getPluginDescription())
        );
        this.terminal.info("強制的なインストールが可能ですが、強制的な操作は予期しない問題を引き起こす可能性があります。");

        signal.setForceInstall(SignalHandlingUtils.askContinue(this.terminal));
    }

    @SignalHandler
    public void onPluginIsIgnored(IgnoredPluginSignal signal)
    {
        this.terminal.warn(
                "%s は ignore としてマークされていますが強制的な操作が可能です。",
                Utils.getPluginString(signal.getPluginDescription())
        );

        this.terminal.warn(ChatColor.DARK_RED + "強制的な操作は予期しない問題を引き起こす可能性があります。");

        signal.setContinueInstall(SignalHandlingUtils.askContinue(this.terminal));
    }

    private void printKeyValue(String key, String value)
    {
        this.terminal.writeLine(ChatColor.DARK_GREEN + key + ChatColor.WHITE + ": " + ChatColor.GREEN + value);
    }

    private void printPluginInfo(PluginDescriptionFile descriptionFile)
    {
        this.printKeyValue("バージョン", descriptionFile.getVersion());
        this.printKeyValue("作者", String.join(", ", descriptionFile.getAuthors()));
        this.printKeyValue("コマンド", String.join(", ", descriptionFile.getCommands().keySet()));
    }

    @SignalHandler
    public void onPluginIsDuplicated(AlreadyInstalledPluginSignal signal)
    {
        this.terminal.warn("%s は既にインストールされています。", signal.getInstalledPlugin().getName());
        this.terminal.infoImplicit("== 既にインストールされているプラグイン ==");
        this.printPluginInfo(signal.getInstalledPlugin());
        this.terminal.infoImplicit("== インストールしようとしているプラグイン ==");
        this.printPluginInfo(signal.getInstallingPlugin());

        signal.setReplacePlugin(SignalHandlingUtils.askContinue(this.terminal));
    }
}
