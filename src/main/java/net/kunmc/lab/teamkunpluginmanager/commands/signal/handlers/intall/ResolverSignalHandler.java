package net.kunmc.lab.teamkunpluginmanager.commands.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.terminal.attributes.AttributeChoice;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.signals.MultiplePluginResolvedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.signals.PluginResolveErrorSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.resolve.signals.PluginResolvingSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.SuccessResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandler;
import org.bukkit.ChatColor;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 依存関係リゾルバのシグナルを処理するハンドラです.
 */
public class ResolverSignalHandler
{
    private final Terminal terminal;

    public ResolverSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onPluginResolving(PluginResolvingSignal signal)
    {
        terminal.writeLine(ChatColor.GREEN + "プラグインを解決しています ...");
    }

    @SignalHandler
    public void onPluginResolveError(PluginResolveErrorSignal signal)
    {
        terminal.error(signal.getError().getCause().getMessage());
    }

    @SignalHandler
    public void onPluginsResolve(MultiplePluginResolvedSignal signal)
    {
        terminal.warn("複数のプラグインが見つかりました。");

        AtomicLong index = new AtomicLong(0);

        LinkedHashMap<String, SuccessResult> keywordToResolveResult = Arrays.stream(signal.getResults().getResults())
                .filter(r -> r instanceof SuccessResult)
                .map(r -> (SuccessResult) r)
                .collect(Collectors.toMap(r -> String.valueOf(index.getAndIncrement()), r -> r, (a, b) -> a, LinkedHashMap::new));
        LinkedHashMap<String, String> keywordToTitle = keywordToResolveResult.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(
                                e.getKey(),
                                e.getValue().getFileName() + "(" + e.getValue().getVersion() + ")"
                        )
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        keywordToTitle.put("a", "自動で最適なプラグインを選択する");

        try
        {
            QuestionResult result = terminal.getInput().showQuestion(
                    "使用するプラグインを選択してください",
                    new AttributeChoice(keywordToTitle),
                    QuestionAttribute.CANCELLABLE
            ).waitAndGetResult();

            if (result.test(QuestionAttribute.CANCELLABLE))
            {
                terminal.error(ChatColor.RED + "インストールをキャンセルしました。");
                signal.setCancel(true);
                return;
            }
            else if (result.getRawAnswer().equalsIgnoreCase("a"))
                return;

            SuccessResult selected = keywordToResolveResult.get(result.getRawAnswer());

            if (selected == null)
            {
                terminal.error("不明な回答が選択されました。");
                signal.setCancel(true);
                return;
            }

            terminal.writeLine(ChatColor.GREEN + selected.getFileName() + "(" + selected.getVersion() + ") が解決されました。");
            signal.setSpecifiedResult(selected);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }
}
