package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.uninstall.signals.UninstallReadySignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals.PluginDisablingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals.PluginRegisteredRecipeSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals.PluginUninstallErrorSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals.PluginUninstallingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.uninstall.signals.PluginUnloadingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import net.kunmc.lab.teamkunpluginmanager.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

public class UninstallerSignalHandler
{
    private final Terminal terminal;

    public UninstallerSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    private static String getErrorCauseMessage(PluginUninstallErrorSignal signal)
    {
        switch (signal.getCause())
        {
            case INTERNAL_CLASS_UNLOAD_FAILED:
                return "クラスのアンロードに失敗しました。";
            case INTERNAL_PLUGIN_DISABLE_FAILED:
                return "プラグインの無効化に失敗しました。";
            default:
                return "不明なエラーが発生しました。";
        }
    }

    private void printUninstallInfo(List<Plugin> uninstallTargets)
    {
        terminal.writeLine(ChatColor.GREEN + "以下のパッケージは「" + ChatColor.RED + "削除" + ChatColor.GREEN + "」されます。");
        terminal.writeLine("  " + uninstallTargets.stream()
                .map(Plugin::getName)
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
    public void onPluginsEnumerated(UninstallReadySignal signal)
    {
        printUninstallInfo(signal.getPlugins());
        signal.setContinueUninstall(pollContinue());
    }

    @SignalHandler
    public void onPluginUninstall(PluginUninstallingSignal signal)
    {
        terminal.writeLine(ChatColor.RED + PluginUtil.getPluginString(signal.getPlugin()) + " をアンインストールしています ...");
    }

    @SignalHandler
    public void onRecipeRemove(PluginRegisteredRecipeSignal.Removing signal)
    {
        terminal.writeLine(ChatColor.RED + PluginUtil.getPluginString(signal.getPlugin()) + " のレシピを削除しています ...");
    }

    @SignalHandler
    public void onDisabling(PluginDisablingSignal.Pre signal)
    {
        terminal.writeLine(ChatColor.GREEN + PluginUtil.getPluginString(signal.getPlugin()) + " のトリガを処理しています ...");
    }

    @SignalHandler
    public void onUnloading(PluginUnloadingSignal.Pre signal)
    {
        terminal.writeLine(ChatColor.RED + PluginUtil.getPluginString(signal.getPlugin()) + " を削除しています ...");
    }

    @SignalHandler
    public void onError(PluginUninstallErrorSignal signal)
    {
        terminal.error(
                ChatColor.GREEN + PluginUtil.getPluginString(signal.getDescription()) + " のアンインストールに失敗しました: ",
                getErrorCauseMessage(signal)
        );
    }
}
