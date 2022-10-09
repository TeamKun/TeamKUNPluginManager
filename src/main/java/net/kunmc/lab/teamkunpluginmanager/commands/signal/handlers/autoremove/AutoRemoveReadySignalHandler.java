package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.autoremove;

import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.autoremove.signals.PluginEnumeratedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.Utils;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class AutoRemoveReadySignalHandler
{
    private final Terminal terminal;

    public AutoRemoveReadySignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    private void printUninstallInfo(List<String> uninstallTargets)
    {
        this.terminal.writeLine(
                ChatColor.GREEN + "以下のプラグインが自動でインストールされましたが、もう必要とされていません：");
        this.terminal.writeLine(
                ChatColor.GREEN + "以下のプラグインは「"
                        + ChatColor.RED + "削除" + ChatColor.GREEN + "」されます。");
        this.terminal.writeLine("  " + uninstallTargets.stream()
                .sorted()
                .collect(Collectors.joining(" ")));
        Utils.printInstallStatistics(terminal, 0, uninstallTargets.size(), 0, 0);
    }

    private boolean pollContinue()
    {
        try
        {
            QuestionResult result = terminal.getInput().showYNQuestion("続行しますか?").waitAndGetResult();
            return result.test(QuestionAttribute.YES);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            terminal.error("不明なエラーが発生しました: " + e.getMessage());
            return false;
        }
    }

    @SignalHandler
    public void onPluginEnumerated(PluginEnumeratedSignal signal)
    {
        this.printUninstallInfo(signal.getTargetPlugins());
        signal.setCancel(!this.pollContinue());
    }
}
