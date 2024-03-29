package org.kunlab.kpm.signal.handlers.clean;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.kunlab.kpm.signal.SignalHandler;
import org.kunlab.kpm.signal.SignalHandlingUtils;
import org.kunlab.kpm.task.tasks.garbage.clean.signal.GarbageEnumeratedSignal;
import org.kunlab.kpm.task.tasks.garbage.search.signals.GarbageSearchingSignal;

import java.util.stream.Collectors;

public class GarbageCleanSignalHandler
{
    // ignore GarbageDeleteSkippedSignal

    private final Terminal terminal;

    public GarbageCleanSignalHandler(Terminal terminal)
    {
        this.terminal = terminal;
    }

    @SignalHandler
    public void onGarbageEnumerated(GarbageEnumeratedSignal signal)
    {
        this.terminal.successImplicit(LangProvider.get("installer.clean.remove"));
        this.terminal.writeLine("  " + signal.getGarbageDatas().stream()
                .sorted()
                .map(path -> path.getFileName().toString())
                .map(path -> path + "/")
                .collect(Collectors.joining(" ")));

        signal.setCancel(!SignalHandlingUtils.askContinue(this.terminal));
    }

    @SignalHandler
    public void onGarbageSearching(GarbageSearchingSignal signal)
    {
        this.terminal.info(LangProvider.get("installer.clean.searching"));
    }
}
