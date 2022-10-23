package net.kunmc.lab.teamkunpluginmanager.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.installer.signals.InvalidKPMInfoFileSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.install.signals.PluginEnablingSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.install.signals.PluginInstallingSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.install.signals.PluginLoadSignal;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.install.signals.PluginRelocatingSignal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandlingUtils;
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
        this.terminal.writeLine(ChatColor.GREEN + signal.getPluginDescription().getName() + " をインストールする準備をしています ...");
    }

    @SignalHandler
    public void onPluginRelocating(PluginRelocatingSignal signal)
    {
        String src = ".../" + signal.getSource().getFileName();
        String dest = ".../" + signal.getTarget().getFileName();
        this.terminal.writeLine(ChatColor.GREEN + src + " を " + dest + " に再配置しています ...");
    }

    @SignalHandler
    public void onPluginLoadPre(PluginLoadSignal.Pre signal)
    {
        this.terminal.writeLine(ChatColor.GREEN +
                PluginUtil.getPluginString(signal.getPluginDescription()) + " を読み込んでいます ...");
    }

    @SignalHandler
    public void onPluginLoading(PluginEnablingSignal.Pre signal)
    {
        this.terminal.writeLine(ChatColor.GREEN +
                PluginUtil.getPluginString(signal.getPlugin()) + " のトリガを処理しています ...");
    }

    @SignalHandler
    public void onInvalidKPMInfoFile(InvalidKPMInfoFileSignal signal)
    {
        this.terminal.warn("プラグイン " + signal.getPlugin() + " はKPM情報ファイル (kpm.yml) を持っていますが、" +
                "KPMが理解できる形式ではありません。");
        this.terminal.info(ChatColor.GRAY + "このファイルを無視して強制的にインストールできますが、" +
                "強制的な操作は予期しない問題を引き起こす可能性があります。");

        signal.setIgnore(SignalHandlingUtils.askContinue(this.terminal));
    }
}
