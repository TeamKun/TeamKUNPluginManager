package net.kunmc.lab.kpm.signal.handlers.uninstall;

import net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals.PluginDisablingSignal;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals.PluginRegisteredRecipeSignal;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals.PluginUninstallErrorSignal;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals.PluginUninstallingSignal;
import net.kunmc.lab.kpm.installer.task.tasks.uninstall.signals.PluginUnloadingSignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.utils.Utils;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;

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
        this.terminal.infoImplicit("%s をアンインストールしています …", Utils.getPluginString(signal.getPlugin()));
    }

    @SignalHandler
    public void onRecipeRemove(PluginRegisteredRecipeSignal.Removing signal)
    {
        if (!this.oneRecipeRemoved)
        {
            this.terminal.infoImplicit("%s のレシピを削除しています …", Utils.getPluginString(signal.getPlugin()));
            this.oneRecipeRemoved = true;
        }
    }

    @SignalHandler
    public void onDisabling(PluginDisablingSignal.Pre signal)
    {
        this.terminal.infoImplicit("%s のトリガを処理しています …", Utils.getPluginString(signal.getPlugin()));
    }

    @SignalHandler
    public void onUnloading(PluginUnloadingSignal.Pre signal)
    {
        this.terminal.infoImplicit(" を削除しています …", Utils.getPluginString(signal.getPlugin()));
    }

    @SignalHandler
    public void onError(PluginUninstallErrorSignal signal)
    {
        this.terminal.error(
                "%s のアンインストールに失敗しました： %s",
                getErrorCauseMessage(signal),
                Utils.getPluginString(signal.getDescription())
        );
    }
}
