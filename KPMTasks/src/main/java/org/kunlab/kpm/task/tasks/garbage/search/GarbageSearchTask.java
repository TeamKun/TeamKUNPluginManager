package org.kunlab.kpm.task.tasks.garbage.search;

import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.interfaces.Installer;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.task.AbstractInstallTask;
import org.kunlab.kpm.task.tasks.garbage.search.signals.GarbageSearchingSignal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 不要なデータの検索を行うタスクです。
 */
public class GarbageSearchTask extends AbstractInstallTask<GarbageSearchArgument, GarbageSearchResult>
{
    private GarbageSearchState status;

    public GarbageSearchTask(@NotNull Installer<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
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
