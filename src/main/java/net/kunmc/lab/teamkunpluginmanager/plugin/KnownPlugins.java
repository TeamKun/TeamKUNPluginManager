package net.kunmc.lab.teamkunpluginmanager.plugin;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class KnownPlugins
{
    private static final ArrayList<KnownPluginEntry> knownPlugins = new ArrayList<>();

    public static KnownPluginEntry getKnown(String name)
    {
        AtomicReference<KnownPluginEntry> result = new AtomicReference<>();
        knownPlugins.stream().parallel()
                .filter(ent -> ent.name.equals(name))
                .forEach(result::set);

        return result.get();

    }

    public static boolean isKnown(String name)
    {
        return getKnown(name) != null;
    }

    public static void addKnownPlugin(KnownPluginEntry entry)
    {
        knownPlugins.add(entry);
    }
}
