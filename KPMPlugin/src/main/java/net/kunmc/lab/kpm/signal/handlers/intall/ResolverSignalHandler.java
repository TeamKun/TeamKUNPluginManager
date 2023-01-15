package net.kunmc.lab.kpm.signal.handlers.intall;

import net.kunmc.lab.kpm.enums.resolver.ErrorCause;
import net.kunmc.lab.kpm.interfaces.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.resolver.result.AbstractSuccessResult;
import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.task.tasks.resolve.signals.MultiplePluginResolvedSignal;
import net.kunmc.lab.kpm.task.tasks.resolve.signals.PluginResolveErrorSignal;
import net.kunmc.lab.kpm.task.tasks.resolve.signals.PluginResolvingSignal;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.terminal.attributes.AttributeChoice;

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
        this.terminal.info("%s を解決しています …", signal.getQuery());
    }

    @SignalHandler
    public void onPluginResolveError(PluginResolveErrorSignal signal)
    {
        ErrorCause errorCause = signal.getError().getCause();
        String message = signal.getError().getMessage() == null ? "": "(" + signal.getError().getMessage() + ")";

        this.terminal.error("%s の解決に失敗しました： %s%s", signal.getQuery(), errorCause, message);
        if (errorCause == ErrorCause.INVALID_CREDENTIAL)
        {
            this.terminal.hint("間違ったトークンが設定されている可能性があります。");
            this.terminal.hint("トークンを再設定するには /kpm register コマンドを使用してください。");
        }
    }

    @SignalHandler
    public void onPluginsResolve(MultiplePluginResolvedSignal signal)
    {
        this.terminal.info("複数のプラグインが見つかりました。");

        if (signal.getSpecifiedResult() != null)
        {
            if (signal.getSpecifiedResult() instanceof AbstractSuccessResult)
            {
                SuccessResult specifiedResult = (SuccessResult) signal.getSpecifiedResult();
                this.terminal.info(
                        "プラグイン %s(%s) が選択されました。",
                        specifiedResult.getFileName(),
                        specifiedResult.getVersion()
                );
            }

            return;
        }

        AtomicLong index = new AtomicLong(0);

        LinkedHashMap<String, SuccessResult> keywordToResolveResult = Arrays.stream(signal.getResults().getResults())
                .filter(r -> r instanceof AbstractSuccessResult)
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
            QuestionResult result = this.terminal.getInput().showQuestion(
                    "使用するプラグインを選択してください",
                    new AttributeChoice(keywordToTitle),
                    QuestionAttribute.CANCELLABLE
            ).waitAndGetResult();

            if (result.test(QuestionAttribute.CANCELLABLE))
            {
                this.terminal.error("解決をキャンセルしました。");
                signal.setCancel(true);
                return;
            }
            else if (result.getRawAnswer().equalsIgnoreCase("a"))
                return;

            SuccessResult selected = keywordToResolveResult.get(result.getRawAnswer());

            if (selected == null)
            {
                this.terminal.error("不明な回答が選択されました。");
                signal.setCancel(true);
                return;
            }

            this.terminal.success(
                    "%s(%s) が解決されました。",
                    selected.getFileName(),
                    selected.getVersion()
            );
            signal.setSpecifiedResult(selected);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }
}
