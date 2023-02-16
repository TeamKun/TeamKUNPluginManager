package org.kunlab.kpm.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.kunlab.kpm.enums.metadata.DependType;
import org.kunlab.kpm.meta.DependencyNode;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginIsDependencySignal;
import org.kunlab.kpm.utils.Utils;

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
        this.terminal.warn("%s は以下のプラグインの依存関係です。", Utils.getPluginString(signal.getPlugin()));
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

        HashMap<String, PluginIsDependencySignal.Operation> optionSelection = new HashMap<>();
        optionSelection.put("u", PluginIsDependencySignal.Operation.UNINSTALL);
        optionSelection.put("d", PluginIsDependencySignal.Operation.DISABLE);
        optionSelection.put("i", PluginIsDependencySignal.Operation.IGNORE);
        optionSelection.put("c", PluginIsDependencySignal.Operation.CANCEL);

        HashMap<String, String> options = new HashMap<>();
        options.put("u", "依存関係をアンインストール");
        options.put("d", "依存関係を無効化");
        options.put("i", ChatColor.DARK_RED + "依存関係を無視");
        options.put("c", ChatColor.RED + "アンインストールをキャンセル");

        try
        {
            QuestionResult result = this.terminal.getInput().showChoiceQuestion(
                            "依存関係の処理方法を選択してください。",
                            options
                    )
                    .waitAndGetResult();

            String selection = result.getRawAnswer();

            return this.lastOperation = optionSelection.get(selection);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            this.terminal.error("不明なエラーが発生しました： %s", e.getMessage());
            return PluginIsDependencySignal.Operation.CANCEL;
        }
    }
}
