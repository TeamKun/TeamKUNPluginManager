package net.kunmc.lab.teamkunpluginmanager.signal.handlers.common;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.install.signals.AlreadyInstalledPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandlingUtils;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
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
    public void onPluginIsIgnored(IgnoredPluginSignal signal)
    {
        this.terminal.warn(PluginUtil.getPluginString(signal.getPluginDescription()) +
                " は ignore としてマークされていますが強制的にインストールが可能です。");

        this.terminal.writeLine(ChatColor.DARK_RED + "W: 強制的なインストールは予期しない問題を引き起こす可能性があります。");

        signal.setCancelInstall(!SignalHandlingUtils.askContinue(this.terminal));
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
        this.terminal.warn(signal.getInstalledPlugin().getName() + " は既にインストールされています。");
        this.terminal.writeLine(ChatColor.BLUE + "== 既にインストールされているプラグイン ==");
        this.printPluginInfo(signal.getInstalledPlugin());
        this.terminal.writeLine(ChatColor.BLUE + "== インストールしようとしているプラグイン ==");
        this.printPluginInfo(signal.getInstallingPlugin());

        signal.setReplacePlugin(SignalHandlingUtils.askContinue(this.terminal));
    }
}
