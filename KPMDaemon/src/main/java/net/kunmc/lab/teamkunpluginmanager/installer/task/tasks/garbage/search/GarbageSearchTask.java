package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.garbage.search;

import net.kunmc.lab.teamkunpluginmanager.installer.AbstractInstaller;
import net.kunmc.lab.teamkunpluginmanager.installer.task.InstallTask;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.garbage.search.signals.GarbageSearchingSignal;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 不要データ検索を行うタスクです。
 */
public class GarbageSearchTask extends InstallTask<GarbageSearchArgument, GarbageSearchResult>
{
    private GarbageSearchState status;

    public GarbageSearchTask(@NotNull AbstractInstaller<?, ?, ?> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());

        this.status = GarbageSearchState.INITIALIZED;
    }

    @Override
    public @NotNull GarbageSearchResult runTask(@NotNull GarbageSearchArgument arguments)
    {

        List<String> excludeList = arguments.getExcludes().stream().parallel()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        List<String> serverPlugins = arguments.getPlugins().stream().parallel()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        GarbageSearchingSignal signal = new GarbageSearchingSignal(arguments.getDataFolder());
        this.postSignal(signal);
        Path dir = signal.getDataFolder();  // dir may be changed by signal handler

        this.status = GarbageSearchState.SEARCHING_GARBAGE;

        List<Path> results;
        try (Stream<Path> stream = Files.list(dir))
        {
            results = stream.parallel()
                    .filter(Files::isDirectory)
                    .filter(file -> !excludeList.contains(file.getFileName().toString().toLowerCase()))
                    .filter(file -> !serverPlugins.contains(file.getFileName().toString().toLowerCase()))
                    .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            return new GarbageSearchResult(false, this.status, GarbageSearchErrorCause.IO_EXCEPTION_OCCURRED);
        }

        return new GarbageSearchResult(this.status, results);
    }
}
