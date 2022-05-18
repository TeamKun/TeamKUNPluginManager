package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.computer;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.InstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.DependencyElement;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is inspired by the class {@link org.bukkit.plugin.SimplePluginManager#loadPlugins(File, List)}
 */
public class DependsComputeOrderPhase extends InstallPhase<DependsComputeOrderArgument, DependsComputeOrderResult>
{
    private DependsComputeOrderState state;

    public DependsComputeOrderPhase(@NotNull InstallProgress<?> progress, @NotNull InstallerSignalHandler signalHandler)
    {
        super(progress, signalHandler);

        this.state = DependsComputeOrderState.INITIALIZED;
    }

    private static DependencyElement get(String name, List<DependencyElement> plugins)
    {
        return plugins.stream().parallel()
                .filter(plugin -> plugin.getPluginDescription().getName().equals(name))
                .findFirst().orElse(null);
    }

    private static boolean contains(String name, List<DependencyElement> plugins)
    {
        return get(name, plugins) != null;
    }

    @Override
    public @NotNull DependsComputeOrderResult runPhase(@NotNull DependsComputeOrderArgument arguments)
    {
        this.state = DependsComputeOrderState.CREATING_DEPENDENCY_MAP;

        List<DependencyElement> plugins = new ArrayList<>(arguments.getCollectedDependencies());

        Map<String, List<String>> dependencies = new HashMap<>();
        Map<String, List<String>> softDependencies = new HashMap<>();

        for (DependencyElement plugin : plugins)
        {
            PluginDescriptionFile description = plugin.getPluginDescription();

            List<String> stagingDependencies = description.getDepend();
            if (!stagingDependencies.isEmpty())
                dependencies.put(description.getName(), new ArrayList<>(stagingDependencies));

            List<String> stagingSoftDependencies = description.getSoftDepend();
            if (!stagingSoftDependencies.isEmpty())
                if (softDependencies.containsKey(description.getName()))
                    softDependencies.get(description.getName()).addAll(stagingSoftDependencies);
                else
                    softDependencies.put(description.getName(), new ArrayList<>(stagingSoftDependencies));

            List<String> loadBeforeSet = description.getLoadBefore();
            if (!loadBeforeSet.isEmpty())
                for (String loadBefore : loadBeforeSet)
                    if (softDependencies.containsKey(loadBefore))
                        softDependencies.get(loadBefore).add(description.getName());
                    else
                        softDependencies.put(
                                loadBefore,
                                new ArrayList<>(Collections.singletonList(description.getName()))
                        );
        }

        this.state = DependsComputeOrderState.COMPUTING_DEPENDENCY_LOAD_ORDER;

        List<DependencyElement> result = new ArrayList<>();

        while (!plugins.isEmpty())
        {
            boolean missingDependency = true;

            Iterator<DependencyElement> iterator = plugins.iterator();
            while (iterator.hasNext())
            {
                DependencyElement plugin = iterator.next();
                String name = plugin.getPluginDescription().getName();

                if (dependencies.containsKey(name))
                {
                    Iterator<String> dependencyIterator = dependencies.get(name).iterator();

                    while (dependencyIterator.hasNext())
                    {
                        String dependency = dependencyIterator.next();

                        if (contains(dependency, result))
                            dependencyIterator.remove();
                        else if (!contains(dependency, plugins))
                            missingDependency = false;
                    }

                    if (!missingDependency)
                    {
                        iterator.remove();
                        softDependencies.remove(name);
                        dependencies.remove(name);

                        // Severe log
                    }

                    if (dependencies.containsKey(name) && dependencies.get(name).isEmpty())
                        dependencies.remove(name);
                }

                if (softDependencies.containsKey(name))
                {
                    softDependencies.get(name).removeIf(softDependency ->
                            !contains(softDependency, result) && !contains(softDependency, plugins));

                    if (softDependencies.get(name).isEmpty())
                        softDependencies.remove(name);
                }

                if (!(dependencies.containsKey(name) || softDependencies.containsKey(name)) &&
                        contains(name, plugins))
                {
                    iterator.remove();
                    missingDependency = false;

                    result.add(plugin);
                }
            }

            if (missingDependency)
            {
                iterator = plugins.iterator();

                while (iterator.hasNext())
                {
                    DependencyElement plugin = iterator.next();
                    String name = plugin.getPluginDescription().getName();

                    if (!dependencies.containsKey(name))
                    {
                        softDependencies.remove(name);
                        iterator.remove();

                        result.add(plugin);
                        break;
                    }
                }
            }
        }

        return new DependsComputeOrderResult(true, this.state, null, result);
    }
}
