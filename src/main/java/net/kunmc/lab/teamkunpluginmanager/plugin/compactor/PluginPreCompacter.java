package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class PluginPreCompacter
{
    private final PluginCompacter compacter;
    private final ArrayList<String> resolveFailed;
    private final ArrayList<CompactBuilder> builder;

    public PluginPreCompacter()
    {
        this.compacter = new PluginCompacter();
        this.resolveFailed = new ArrayList<>();
        this.builder = new ArrayList<>();
    }

    public CompactBuilder getBuilderFromName(String name)
    {
        AtomicReference<CompactBuilder> result = new AtomicReference<>();
        builder.forEach(compactBuilder -> {
            if (compactBuilder.getPre().pluginName.equalsIgnoreCase(name))
                result.set(compactBuilder);
        });

        return result.get();
    }

    public void fixUrl(String name, String url)
    {
        if (!resolveFailed.contains(name))
            return;
        this.builder.forEach(compactBuilder -> {
            CompactBuilder builder = getBuilderFromName(name);
            if (builder == null)
                return;
            builder.applyUrl(url);
        });
    }

    public String nextUrlError()
    {
        return resolveFailed.get(resolveFailed.size() - 1);
    }

    public void addAll(String[] target)
    {
        Arrays.stream(target).parallel()
                .forEach(s -> {
                    CompactBuilder builder = new CompactBuilder(this.compacter);
                    builder.addPlugin(s);
                    this.builder.add(builder);
                    if (builder.isResolveFailed())
                        this.resolveFailed.add(s);
                });
        applyAll();
    }

    public void applyAll()
    {
        this.builder.forEach(b -> {
            if (b.isResolveFailed())
                return;
            b.build();
            builder.remove(b);
        });
    }
}
