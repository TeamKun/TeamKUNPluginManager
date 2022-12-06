package net.kunmc.lab.kpm.signal.handlers.uninstall;

import net.kunmc.lab.kpm.installer.impls.uninstall.signals.PluginIsDependencySignal;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.utils.PluginUtil;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.plugin.Plugin;

import java.util.stream.Collectors;

public class PluginIsDependencySignalHandler
{
    private final Terminal terminal;
    private boolean yesForAll = false;

    public PluginIsDependencySignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onPluginIsDependency(PluginIsDependencySignal signal)
    {
        this.terminal.warn(PluginUtil.getPluginString(signal.getPlugin()) + " は以下のプラグインの依存関係です。");
        this.terminal.writeLine("  " + signal.getDependedBy().stream()
                .map(Plugin::getName)
                .sorted()
                .collect(Collectors.joining(" ")));
        this.terminal.warn("このプラグインのアンインストールにより、これらのプラグインが動作しなくなる可能性があります。");

        boolean uninstallThem = this.pollUninstallDeps();

        signal.setForceUninstall(uninstallThem);
    }

    private boolean pollUninstallDeps()
    {
        if (this.yesForAll)
            return true;

        try
        {
            QuestionResult result = this.terminal.getInput().showYNQuestion(
                    "これらのプラグインもアンインストールを行いますか?",
                    QuestionAttribute.APPLY_FOR_ALL
            ).waitAndGetResult();

            if (result.test(QuestionAttribute.APPLY_FOR_ALL))
                this.yesForAll = true;

            return result.test(QuestionAttribute.YES);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            this.terminal.error("不明なエラーが発生しました: " + e.getMessage());
            return false;
        }
    }
}
