package net.kunmc.lab.teamkunpluginmanager.plugin.compactor;

import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PluginPreCompacter
{
    private final PluginCompacter compacter;
    private final ArrayList<String> resolveFailed;
    private ArrayList<CompactBuilder> builder;

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
            resolveFailed.remove(name);
        });
    }

    public String nextUrlError()
    {
        if (resolveFailed.size() == 0)
            return null;
        return resolveFailed.get(resolveFailed.size() - 1);
    }

    public void addAll(String[] target)
    {
        Arrays.stream(target).parallel()
                .forEach(s -> {
                    CompactBuilder builder = new CompactBuilder(this.compacter);
                    builder.addPlugin(s);
                    builder.applyConfig(PluginUtil.getConfig(Bukkit.getPluginManager().getPlugin(s)));
                    this.builder.add(builder);
                    if (builder.isResolveFailed())
                        this.resolveFailed.add(s);
                });
        applyAll();
    }

    public boolean isErrors()
    {
        return this.nextUrlError() != null;
    }

    public PluginCompacter getCompacter()
    {
        return compacter;
    }

    public void applyAll()
    {
        this.builder = this.builder.parallelStream().filter(b -> {
            if (b.isResolveFailed())
                return true;
            compacter.apply(b.build());
            return false;
        }).collect(Collectors.toCollection(ArrayList::new));

    }

    public void bundleConfig(String name, Map<String, Object> config)
    {
        CompactBuilder builder = getBuilderFromName(name);
        if (builder == null)
            return;
        builder.applyConfig(config);
    }


}
