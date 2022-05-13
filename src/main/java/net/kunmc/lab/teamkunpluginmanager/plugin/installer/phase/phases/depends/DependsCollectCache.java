package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.depends;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final HashMap<String, Path> enumeratedDependencies;

    @NotNull
    private String pluginName;

    private DependsCollectCache(@NotNull String installId)
    {
        this.installId = installId;
        this.enumeratedDependencies = new HashMap<>();
        this.pluginName = "undefined-" + installId;

        this.cacheFile = Paths.get(".cache", installId, installId + ".kpmcache").toFile();
    }

    public static DependsCollectCache of(@NotNull String installId)
    {
        File cacheFile = Paths.get(".cache", installId, installId + ".kpmcache").toFile();

        if (!cacheFile.exists())
            return new DependsCollectCache(installId);

        try (FileReader fis = new FileReader(cacheFile))
        {
            return gson.fromJson(fis, DependsCollectCache.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new DependsCollectCache(installId);
        }
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
        return enumeratedDependencies.containsValue(null);
    }

    public void onCollect(@NotNull String dependencyName, @Nullable Path dependencyPath)
    {
        if (enumeratedDependencies.containsKey(dependencyName))
            enumeratedDependencies.put(dependencyName, dependencyPath);
    }

    public List<String> getCollectedDependencies()
    {
        return this.enumeratedDependencies.entrySet().stream().parallel()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey)
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

    public DependsCollectCache update()
    {
        return of(this.installId);
    }
}
