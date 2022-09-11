package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.common;

import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.install.signals.AlreadyInstalledPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.assertion.IgnoredPluginSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
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
        terminal.warn(PluginUtil.getPluginString(signal.getPluginDescription()) +
                " は ignore としてマークされていますが強制的にインストールが可能です。");

        terminal.writeLine(ChatColor.DARK_RED + "W: 強制的なインストールは予期しない問題を引き起こす可能性があります。");

        try
        {
            QuestionResult result = terminal.getInput().showYNQuestion("続行しますか?")
                    .waitAndGetResult();

            boolean continueInstall = result.test(QuestionAttribute.YES);

            signal.setCancelInstall(!continueInstall);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

    private void printKeyValue(String key, String value)
    {
        terminal.writeLine(ChatColor.DARK_GREEN + key + ChatColor.WHITE + ": " + ChatColor.GREEN + value);
    }

    private void printPluginInfo(PluginDescriptionFile descriptionFile)
    {
        printKeyValue("バージョン", descriptionFile.getVersion());
        printKeyValue("作者", String.join(", ", descriptionFile.getAuthors()));
        printKeyValue("コマンド", String.join(", ", descriptionFile.getCommands().keySet()));
    }

    @SignalHandler
    public void onPluginIsDuplicated(AlreadyInstalledPluginSignal signal)
    {
        terminal.warn(signal.getInstalledPlugin().getName() + " は既にインストールされています。");
        terminal.writeLine(ChatColor.BLUE + "== 既にインストールされているプラグイン ==");
        printPluginInfo(signal.getInstalledPlugin());
        terminal.writeLine(ChatColor.BLUE + "== インストールしようとしているプラグイン ==");
        printPluginInfo(signal.getInstallingPlugin());

        try
        {
            QuestionResult result = terminal.getInput().showYNQuestion("続行しますか?")
                    .waitAndGetResult();

            boolean continueInstall = result.test(QuestionAttribute.YES);

            signal.setReplacePlugin(continueInstall);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }
}
