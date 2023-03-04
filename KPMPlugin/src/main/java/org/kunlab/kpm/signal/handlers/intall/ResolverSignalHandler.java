package org.kunlab.kpm.signal.handlers.intall;

import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionAttribute;
import net.kunmc.lab.peyangpaperutils.lib.terminal.QuestionResult;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.terminal.attributes.AttributeChoice;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.lang.MsgArgs;
import org.kunlab.kpm.resolver.ErrorCause;
import org.kunlab.kpm.resolver.interfaces.result.SuccessResult;
import org.kunlab.kpm.resolver.result.AbstractSuccessResult;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.task.tasks.resolve.signals.MultiplePluginResolvedSignal;
import org.kunlab.kpm.task.tasks.resolve.signals.PluginResolveErrorSignal;
import org.kunlab.kpm.task.tasks.resolve.signals.PluginResolvingSignal;
import org.kunlab.kpm.utils.KPMCollectors;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 依存関係リゾルバのシグナルを処理するハンドラです.
 */
public class ResolverSignalHandler
{
    private final KPMRegistry registry;
    private final Terminal terminal;

    public ResolverSignalHandler(KPMRegistry registry, Terminal terminal)
    {
        this.registry = registry;
        this.terminal = terminal;
    }

    @SignalHandler
    public void onPluginResolving(PluginResolvingSignal signal)
    {
        this.terminal.info(LangProvider.get(
                "tasks.resolve.resolving",
                MsgArgs.of("query", signal.getQuery())
        ));
    }

    @SignalHandler
    public void onPluginResolveError(PluginResolveErrorSignal signal)
    {
        ErrorCause errorCause = signal.getError().getCause();
        String message = signal.getError().getMessage() == null ? "": "(" + signal.getError().getMessage() + ")";

        switch (errorCause)
        {
            case PLUGIN_NOT_FOUND:
                this.terminal.error(LangProvider.get("tasks.resolve.failed.not_found"));
                break;
            case ASSET_NOT_FOUND:
                this.terminal.error(LangProvider.get("tasks.resolve.failed.asset_not_found"));
                break;
            case RESOLVER_MISMATCH:
                this.terminal.error(LangProvider.get("tasks.resolve.failed.resolver_mismatch"));
                break;
            case SERVER_RESPONSE_ERROR:
            case SERVER_RESPONSE_MALFORMED:
                this.terminal.error(LangProvider.get(
                        "tasks.resolve.failed.server_error",
                        MsgArgs.of("message", message)
                ));
                break;
            // noinspection FallThroughInSwitchStatement
            case INVALID_CREDENTIAL:
                this.terminal.hint(LangProvider.get("tasks.resolve.failed.wrong_token"));
                this.terminal.hint(LangProvider.get(
                        "tasks.resolve.failed.wrong_token.suggest",
                        MsgArgs.of("command", "/kpm register")
                ));
                // no break
            default:
                this.terminal.error(LangProvider.get(
                        "tasks.resolve.failed",
                        MsgArgs.of("query", signal.getQuery())
                                .add("cause", errorCause)
                                .add("message", message)
                ));
        }
    }

    @SignalHandler
    public void onPluginsResolve(MultiplePluginResolvedSignal signal)
    {
        this.terminal.info(LangProvider.get(
                "tasks.resolve.multi",
                MsgArgs.of("query", signal.getQuery())
        ));

        if (signal.getSpecifiedResult() != null)
        {
            if (signal.getSpecifiedResult() instanceof AbstractSuccessResult)
            {
                SuccessResult specifiedResult = (SuccessResult) signal.getSpecifiedResult();
                this.terminal.info(LangProvider.get(
                        "tasks.resolve.multi.specified",
                        MsgArgs.of("name", specifiedResult.getFileName())
                                .add("version", specifiedResult.getVersion())
                ));
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
                .collect(KPMCollectors.toMap(LinkedHashMap::new));
        keywordToTitle.put("a", LangProvider.get("tasks.resolve.multi.choices.auto"));

        try
        {
            QuestionResult result = this.terminal.getInput().showQuestion(
                    LangProvider.get("tasks.resolve.multi.choice"),
                    new AttributeChoice(keywordToTitle),
                    QuestionAttribute.CANCELLABLE
            ).waitAndGetResult();

            if (result.test(QuestionAttribute.CANCELLABLE))
            {
                this.terminal.error(LangProvider.get("tasks.resolve.multi.cancel", MsgArgs.of("name", "解決")));
                signal.setCancel(true);
                return;
            }
            else if (result.getRawAnswer().equalsIgnoreCase("a"))
                return;

            SuccessResult selected = keywordToResolveResult.get(result.getRawAnswer());
            assert selected != null;

            this.terminal.info(LangProvider.get(
                    "tasks.resolve.multi.specified",
                    MsgArgs.of("name", selected.getFileName())
                            .add("version", selected.getVersion())
            ));

            signal.setSpecifiedResult(selected);
        }
        catch (InterruptedException ex)
        {
            this.registry.getExceptionHandler().report(ex);
        }
    }
}
