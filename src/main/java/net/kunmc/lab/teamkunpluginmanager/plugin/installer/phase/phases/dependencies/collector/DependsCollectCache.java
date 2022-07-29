package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.collector;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies.DependencyElement;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class DependsCollectCache
{
    private static final Gson gson = new Gson();

    @NotNull
    private final File cacheFile;
    @NotNull
    private final String installId;
    @NotNull
    @Getter(AccessLevel.PRIVATE)
    private final HashMap<String, DependencyElement> enumeratedDependencies;

    @NotNull
    private String pluginName;

    public DependsCollectCache(InstallProgress<?> progress)
    {
        this.installId = progress.getInstallActionID();
        this.enumeratedDependencies = new HashMap<>();
        this.pluginName = "undefined-" + this.installId;

        this.cacheFile = progress.getInstallTempDir().resolve(this.installId + ".kpmcache").toFile();
    }

    public void addDependency(@NotNull String dependencyName)
    {
        if (!enumeratedDependencies.containsKey(dependencyName))
            enumeratedDependencies.put(dependencyName, null);
    }

    public boolean isCollected(@NotNull String dependencyName)
    {
        return enumeratedDependencies.containsKey(dependencyName);
    }

    public boolean isErrors()
    {
        return !enumeratedDependencies.containsValue(null);
    }

    public void onCollect(@NotNull String dependencyName, DependencyElement dependencyElement)
    {
        if (enumeratedDependencies.containsKey(dependencyName))
            enumeratedDependencies.put(dependencyName, dependencyElement);
    }

    public List<DependencyElement> getCollectedDependencies()
    {
        return this.enumeratedDependencies.entrySet().stream().parallel()
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<String> getCollectFailedDependencies()
    {
        return this.enumeratedDependencies.entrySet().stream().parallel()
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public boolean save()
    {
        try (FileWriter fos = new FileWriter(this.cacheFile))
        {
            fos.write(gson.toJson(this));
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
