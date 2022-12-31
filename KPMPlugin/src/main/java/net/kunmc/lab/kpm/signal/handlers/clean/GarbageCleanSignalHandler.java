package net.kunmc.lab.kpm.signal.handlers.clean;

import net.kunmc.lab.kpm.signal.SignalHandler;
import net.kunmc.lab.kpm.signal.SignalHandlingUtils;
import net.kunmc.lab.kpm.task.tasks.garbage.clean.signal.GarbageEnumeratedSignal;
import net.kunmc.lab.kpm.task.tasks.garbage.search.signals.GarbageSearchingSignal;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;

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
        this.terminal.successImplicit("以下の未使用データは「" + ChatColor.RED + "削除" + ChatColor.RESET + "」されます。");
        this.terminal.writeLine("  " + signal.getGarbageDatas().stream()
                .sorted()
                .map(path -> path.getFileName().toString())
                .collect(Collectors.joining(" ")));

        signal.setCancel(!SignalHandlingUtils.askContinue(this.terminal));
    }

    @SignalHandler
    public void onGarbageSearching(GarbageSearchingSignal signal)
    {
        this.terminal.info("不要データを検索しています …");
    }
}
