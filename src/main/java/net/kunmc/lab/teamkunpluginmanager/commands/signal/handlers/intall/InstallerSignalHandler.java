package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.signals.PluginEnablingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.signals.PluginInstallingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.signals.PluginLoadSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.install.signals.PluginRelocatingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.ChatColor;

/**
 * インストーラのシグナルをハンドルするハンドラです.
 */
public class InstallerSignalHandler
{
    private final Terminal terminal;

    public InstallerSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    // Ignore PluginOnLoadRunningSignal

    @SignalHandler
    public void onPluginInstallStart(PluginInstallingSignal signal)
    {
        terminal.writeLine(ChatColor.GREEN + signal.getPluginDescription().getName() + " をインストールする準備をしています ...");
    }

    @SignalHandler
    public void onPluginRelocating(PluginRelocatingSignal signal)
    {
        String src = ".../" + signal.getSource().getFileName();
        String dest = ".../" + signal.getTarget().getFileName();
        terminal.writeLine(ChatColor.GREEN + src + " を " + dest + " に再配置しています ...");
    }

    @SignalHandler
    public void onPluginLoadPre(PluginLoadSignal.Pre signal)
    {
        terminal.writeLine(ChatColor.GREEN +
                PluginUtil.getPluginString(signal.getPluginDescription()) + " を読み込んでいます ...");
    }

    @SignalHandler
    public void onPluginLoading(PluginEnablingSignal.Pre signal)
    {
        terminal.writeLine(ChatColor.GREEN +
                PluginUtil.getPluginString(signal.getPlugin()) + " のトリガを処理しています ...");
    }
}
