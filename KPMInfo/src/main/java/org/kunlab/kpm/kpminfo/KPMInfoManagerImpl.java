package org.kunlab.kpm.kpminfo;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.kpminfo.interfaces.KPMInfoManager;
import org.kunlab.kpm.utils.PluginUtil;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.zip.ZipFile;

public class KPMInfoManagerImpl implements KPMInfoManager
{
    private final KPMRegistry registry;
    private final HashMap<String, KPMInformationFile> lookupNames;

    public KPMInfoManagerImpl(KPMRegistry registry)
    {
        this.registry = registry;
        this.lookupNames = new HashMap<>();
    }

    @Override
    @Nullable
    public KPMInformationFile loadInfo(@NotNull Path path, @NotNull PluginDescriptionFile descriptionFile) throws
            FileNotFoundException, InvalidInformationFileException
    {
        KPMInformationFile info = KPMInfoParser.load(this.registry, path);
        this.lookupNames.put(descriptionFile.getName(), info);

        if (PluginUtil.isPluginLoaded(descriptionFile.getName()))
            info.getHooks().bakeHooks(this.registry);

        return info;
    }

    @Override
    @Nullable
    public KPMInformationFile getInfo(@NotNull Plugin plugin)
    {
        return this.lookupNames.get(plugin.getDescription().getName());
    }

    @Override
    @Nullable
    public KPMInformationFile getInfo(@NotNull String pluginName)
    {
        return this.lookupNames.get(pluginName);
    }

    @Override
    @Nullable
    public KPMInformationFile getOrLoadInfo(@NotNull Plugin plugin)
    {
        KPMInformationFile info = this.getInfo(plugin);
        if (info != null)
            return info;

        if (!this.hasInfo(plugin))
            return null;

        try
        {
            return this.loadInfo(PluginUtil.getFile(plugin).toPath(), plugin.getDescription());
        }
        catch (FileNotFoundException | InvalidInformationFileException e)
        {
            this.registry.getExceptionHandler().report(e);
            return null;
        }
    }

    @Override
    public boolean hasInfo(@NotNull Plugin plugin)
    {
        if (this.lookupNames.containsKey(plugin.getDescription().getName()))
            return true;

        return this.hasInfo(PluginUtil.getFile(plugin).toPath());
    }

    @Override
    public boolean hasInfo(@NotNull Path pluginFile)
    {
        try (ZipFile zipFile = new ZipFile(pluginFile.toFile()))
        {
            return zipFile.getEntry("kpm.yml") != null;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    public void removeInfo(@NotNull Plugin plugin)
    {
        this.lookupNames.remove(plugin.getDescription().getName());
    }
}
