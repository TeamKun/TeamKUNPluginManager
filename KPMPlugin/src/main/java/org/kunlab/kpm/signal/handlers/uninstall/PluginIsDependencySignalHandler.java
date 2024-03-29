package org.kunlab.kpm.signal.handlers.uninstall;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.meta.DependType;
import org.kunlab.kpm.meta.interfaces.DependencyNode;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.task.tasks.uninstall.signals.PluginIsDependencySignal;
import org.kunlab.kpm.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PluginIsDependencySignalHandler
{
    private final KPMRegistry registry;
    private final Terminal terminal;

    private PluginIsDependencySignal.Operation lastOperation;

    public PluginIsDependencySignalHandler(KPMRegistry registry, Terminal terminal)
    {
        this.registry = registry;
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
        this.terminal.warn(LangProvider.get(
                "tasks.uninstall.dependency.notify",
                MsgArgs.of("plugin", Utils.getPluginString(signal.getPlugin()))
        ));
        this.terminal.writeLine("  " + signal.getDependedBy().stream()
                .map(PluginIsDependencySignalHandler::dependencyNameMapper)
                .sorted()
                .collect(Collectors.joining(" ")));
        this.terminal.warn(LangProvider.get("tasks.uninstall.dependency.warn"));

        PluginIsDependencySignal.Operation operation = this.pollUninstallDeps();
        signal.setOperation(operation);
    }

    private PluginIsDependencySignal.Operation pollUninstallDeps()
    {
        if (this.lastOperation != null)  // This is not the first time to ask so auto select the last operation
            return this.lastOperation;

        Map<String, PluginIsDependencySignal.Operation> optionSelection = new HashMap<>();
        optionSelection.put("u", PluginIsDependencySignal.Operation.UNINSTALL);
        optionSelection.put("d", PluginIsDependencySignal.Operation.DISABLE);
        optionSelection.put("i", PluginIsDependencySignal.Operation.IGNORE);
        optionSelection.put("c", PluginIsDependencySignal.Operation.CANCEL);

        HashMap<String, String> options = new HashMap<>();
        options.put("u", LangProvider.get("tasks.uninstall.dependency.choice.uninstall"));
        options.put("d", LangProvider.get("tasks.uninstall.dependency.choice.disable"));
        options.put("i", LangProvider.get("tasks.uninstall.dependency.choice.ignore"));
        options.put("c", LangProvider.get("tasks.uninstall.dependency.choice.cancel"));

        try
        {
            QuestionResult result = this.terminal.getInput().showChoiceQuestion(
                            LangProvider.get("tasks.uninstall.dependency.choice"),
                            options
                    )
                    .waitAndGetResult();

            String selection = result.getRawAnswer();

            return this.lastOperation = optionSelection.get(selection);
        }
        catch (InterruptedException e)
        {
            this.registry.getExceptionHandler().report(e);
            this.terminal.error(LangProvider.get(
                    "tasks.uninstall.dependency.error",
                    MsgArgs.of("error", e.getMessage())
            ));
            return PluginIsDependencySignal.Operation.CANCEL;
        }
    }
}
