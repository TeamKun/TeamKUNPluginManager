package net.kunmc.lab.kpm.signal.handlers.uninstall;

import net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals.PluginDisablingSignal;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals.PluginRegisteredRecipeSignal;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals.PluginUninstallErrorSignal;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals.PluginUninstallingSignal;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals.PluginUnloadingSignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.utils.Utils;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;

public class UninstallerSignalHandler
{
    private final Terminal terminal;
    private boolean oneRecipeRemoved;

    public UninstallerSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
        this.oneRecipeRemoved = false;
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
        this.terminal.writeLine(ChatColor.RED + Utils.getPluginString(signal.getPlugin()) + " をアンインストールしています ...");
    }

    @SignalHandler
    public void onRecipeRemove(PluginRegisteredRecipeSignal.Removing signal)
    {
        if (!this.oneRecipeRemoved)
        {
            this.terminal.writeLine(ChatColor.RED + Utils.getPluginString(signal.getPlugin()) + " のレシピを削除しています ...");
            this.oneRecipeRemoved = true;
        }
    }

    @SignalHandler
    public void onDisabling(PluginDisablingSignal.Pre signal)
    {
        this.terminal.writeLine(ChatColor.GREEN + Utils.getPluginString(signal.getPlugin()) + " のトリガを処理しています ...");
    }

    @SignalHandler
    public void onUnloading(PluginUnloadingSignal.Pre signal)
    {
        this.terminal.writeLine(ChatColor.RED + Utils.getPluginString(signal.getPlugin()) + " を削除しています ...");
    }

    @SignalHandler
    public void onError(PluginUninstallErrorSignal signal)
    {
        this.terminal.error(
                ChatColor.GREEN + Utils.getPluginString(signal.getDescription()) + " のアンインストールに失敗しました: ",
                getErrorCauseMessage(signal)
        );
    }
}
