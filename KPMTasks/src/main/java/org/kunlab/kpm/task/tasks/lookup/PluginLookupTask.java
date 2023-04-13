package org.kunlab.kpm.task.tasks.lookup;

import net.kunmc.lab.peyangpaperutils.collectors.ExCollectors;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.interfaces.Installer;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.resolver.QueryContextParser;
import org.kunlab.kpm.resolver.interfaces.QueryContext;
import org.kunlab.kpm.task.AbstractInstallTask;
import org.kunlab.kpm.versioning.Version;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * サーバに導入されているプラグインを検索するタスクです。
 * クエリを指定し、マッチするプラグインを検索します。
 */
public class PluginLookupTask extends AbstractInstallTask<LookupArgument, LookupResult>
{
    private LookupState state;

    public PluginLookupTask(@NotNull Installer<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());

        this.state = LookupState.INITIALIZED;
    }

    private static Map<String, Plugin> lookupAll(QueryContext[] queries, String[] queryStrings)
    {
        AtomicInteger count = new AtomicInteger(0);
        return Arrays.stream(queries)
                .map(query -> Pair.of(queryStrings[count.getAndIncrement()], lookupOne(query)))
                .collect(ExCollectors.toPairMap(LinkedHashMap::new));
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
                    .map(QueryContextParser::fromString)
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
        Map<String, Plugin> result = lookupAll(queries, queryStrs);

        return new LookupResult(true, this.state, result);
    }
}
