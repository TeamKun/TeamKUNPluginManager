package org.kunlab.kpm.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.interfaces.installer.signals.InvalidKPMInfoFileSignal;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.SignalHandlingUtils;
import org.kunlab.kpm.task.tasks.install.signals.PluginEnablingSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginInstallingSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginLoadSignal;
import org.kunlab.kpm.task.tasks.install.signals.PluginRelocatingSignal;
import org.kunlab.kpm.utils.Utils;

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
        this.terminal.infoImplicit("%s をインストールする準備をしています …", signal.getPluginDescription().getName());
    }

    @SignalHandler
    public void onPluginRelocating(PluginRelocatingSignal signal)
    {
        String src = "…/" + signal.getSource().getFileName();
        String dest = "…/" + signal.getTarget().getFileName();
        this.terminal.infoImplicit(
                "%s を %s に再配置しています …",
                src,
                dest
        );
    }

    @SignalHandler
    public void onPluginLoadPre(PluginLoadSignal.Pre signal)
    {
        this.terminal.infoImplicit("%s を読み込んでいます …", Utils.getPluginString(signal.getPluginDescription()));
    }

    @SignalHandler
    public void onPluginLoading(PluginEnablingSignal.Pre signal)
    {
        this.terminal.infoImplicit("%s のトリガを処理しています …", Utils.getPluginString(signal.getPlugin()));
    }

    @SignalHandler
    public void onInvalidKPMInfoFile(InvalidKPMInfoFileSignal signal)
    {
        this.terminal.warn(
                "プラグイン %s は KPM 情報ファイル (kpm.yml) を持っていますが、KPMが理解できる形式ではありません。",
                signal.getDescriptionFile().getName()
        );
        this.terminal.hint(
                "このファイルを無視して強制的にインストールできますが、強制的な操作は予期しない問題を引き起こす可能性があります。"
        );

        signal.setIgnore(SignalHandlingUtils.askContinue(this.terminal));
    }
}
