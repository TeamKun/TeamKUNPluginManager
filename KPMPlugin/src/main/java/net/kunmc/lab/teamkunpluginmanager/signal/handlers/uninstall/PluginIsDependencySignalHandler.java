package net.kunmc.lab.teamkunpluginmanager.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.uninstall.signals.PluginIsDependencySignal;
import net.kunmc.lab.teamkunpluginmanager.signal.SignalHandler;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.plugin.Plugin;

import java.util.List;
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
        terminal.warn(PluginUtil.getPluginString(signal.getPlugin()) + " は以下のプラグインの依存関係です。");
        terminal.writeLine("  " + signal.getDependedBy().stream()
                .map(Plugin::getName)
                .sorted()
                .collect(Collectors.joining(" ")));
        terminal.warn("このプラグインのアンインストールにより、これらのプラグインが動作しなくなる可能性があります。");

        boolean uninstallThem = pollUninstallDeps(signal.getDependedBy());

        signal.setForceUninstall(uninstallThem);
    }

    private boolean pollUninstallDeps(List<Plugin> dependencies)
    {
        if (yesForAll)
            return true;

        try
        {
            QuestionResult result = terminal.getInput().showYNQuestion(
                    "これらのプラグインもアンインストールを行いますか?",
                    QuestionAttribute.APPLY_FOR_ALL
            ).waitAndGetResult();

            if (result.test(QuestionAttribute.APPLY_FOR_ALL))
                yesForAll = true;

            return result.test(QuestionAttribute.YES);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            terminal.error("不明なエラーが発生しました: " + e.getMessage());
            return false;
        }
    }
}
