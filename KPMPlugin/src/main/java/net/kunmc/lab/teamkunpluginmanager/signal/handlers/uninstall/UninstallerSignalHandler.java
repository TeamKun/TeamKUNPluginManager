package net.kunmc.lab.teamkunpluginmanager.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.signals.PluginDisablingSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.signals.PluginRegisteredRecipeSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.signals.PluginUninstallErrorSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.signals.PluginUninstallingSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.uninstall.signals.PluginUnloadingSignal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.ChatColor;

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
