package net.kunmc.lab.kpm.installer.task.tasks.lookup;

import net.kunmc.lab.kpm.installer.task.InstallTask;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.utils.KPMCollectors;
import net.kunmc.lab.kpm.versioning.Version;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * サーバに導入されているプラグインを検索するタスクです。
 * クエリを指定し、マッチするプラグインを検索します。
 */
public class PluginLookupTask extends InstallTask<LookupArgument, LookupResult>
{
    private LookupState state;

    public PluginLookupTask(@NotNull PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());

        this.state = LookupState.INITIALIZED;
    }

    private static LinkedHashMap<String, Plugin> lookupAll(QueryContext[] queries, String[] queryStrings)
    {
        AtomicInteger count = new AtomicInteger(0);
        return Arrays.stream(queries)
                .map(query -> Pair.of(queryStrings[count.getAndIncrement()], lookupOne(query)))
                .collect(KPMCollectors.toPairMap(LinkedHashMap::new));
    }

    private static Plugin lookupOne(QueryContext query)
    {
        Plugin plugin;
        if ((plugin = Bukkit.getPluginManager().getPlugin(query.getQuery())) == null)
            return null;

        if (query.getVersion() == null)
            return plugin;

        String pluginVersionStr = plugin.getDescription().getVersion();
        if (!Version.isValidVersionString(pluginVersionStr))
            return null;

        Version pluginVersion = Version.of(pluginVersionStr);

        if (!query.getVersion().isEqualTo(pluginVersion))
            return null;

        return plugin;
    }

    private static QueryContext[] parseQueries(String[] queries)
    {
        try
        {
            QueryContext[] result = Arrays.stream(queries)
                    .map(QueryContext::fromString)
                    .toArray(QueryContext[]::new);

            return result.length == queries.length ? result: null;
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    @Override
    public @NotNull LookupResult runTask(@NotNull LookupArgument arguments)
    {
        this.state = LookupState.QUERY_PARSING;
        String[] queryStrs = arguments.getQueries();
        QueryContext[] queries = parseQueries(queryStrs);

        if (queries == null)
            return new LookupResult(false, this.state, LookupErrorCause.INVALID_QUERY);

        this.state = LookupState.PLUGIN_LOOKUP;
        LinkedHashMap<String, Plugin> result = lookupAll(queries, queryStrs);

        return new LookupResult(true, this.state, result);
    }
}
