package net.kunmc.lab.kpm.signal.handlers.uninstall;

import net.kunmc.lab.kpm.installer.impls.uninstall.signals.PluginIsDependencySignal;
import net.kunmc.lab.kpm.meta.DependType;
import net.kunmc.lab.kpm.meta.DependencyNode;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.utils.Utils;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.stream.Collectors;

public class PluginIsDependencySignalHandler
{
    private final Terminal terminal;

    private PluginIsDependencySignal.Operation lastOperation;

    public PluginIsDependencySignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    private static String dependencyNameMapper(DependencyNode node)
    {
        boolean isSoft = node.getDependType() == DependType.SOFT_DEPEND;

        return (isSoft ? ChatColor.YELLOW: ChatColor.RED) + node.getDependsOn();
    }

    @SignalHandler
    public void onPluginIsDependency(PluginIsDependencySignal signal)
    {
        this.terminal.warn(Utils.getPluginString(signal.getPlugin()) + " は以下のプラグインの依存関係です。");
        this.terminal.writeLine("  " + signal.getDependedBy().stream()
                .map(PluginIsDependencySignalHandler::dependencyNameMapper)
                .sorted()
                .collect(Collectors.joining(" ")));
        this.terminal.warn("このプラグインのアンインストールにより、これらのプラグインが動作しなくなる可能性があります。");

        PluginIsDependencySignal.Operation operation = this.pollUninstallDeps();
        signal.setOperation(operation);
    }

    private PluginIsDependencySignal.Operation pollUninstallDeps()
    {
        if (this.lastOperation != null)  // This is not the first time to ask so auto select the last operation
            return this.lastOperation;

        HashMap<String, String> options = new HashMap<>();
        options.put(PluginIsDependencySignal.Operation.UNINSTALL.name().toLowerCase(), "依存関係をアンインストール");
        options.put(PluginIsDependencySignal.Operation.DISABLE.name().toLowerCase(), "依存関係を無効化");
        options.put(PluginIsDependencySignal.Operation.IGNORE.name().toLowerCase(), "依存関係を無視");
        options.put("c", "アンインストールをキャンセル");

        try
        {
            QuestionResult result = this.terminal.getInput().showChoiceQuestion(
                            "依存関係の処理方法を選択してください。",
                            options
                    )
                    .waitAndGetResult();

            String selection = result.getRawAnswer();

            if (selection.equalsIgnoreCase("c"))  // Special bypass for cancel (it is easy to type "c" than "cancel")
                return PluginIsDependencySignal.Operation.CANCEL;

            // Value is validated by the terminal so it's safe to use valueOf
            this.lastOperation = PluginIsDependencySignal.Operation.valueOf(selection.toUpperCase());
            return this.lastOperation;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            this.terminal.error("不明なエラーが発生しました: " + e.getMessage());
            return PluginIsDependencySignal.Operation.CANCEL;
        }
    }
}
