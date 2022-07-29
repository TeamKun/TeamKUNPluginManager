package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.dependencies;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Value
@AllArgsConstructor
public class DependencyElement
{
    @Expose
    @NotNull
    String pluginName;
    @Expose
    @NotNull
    Path pluginPath;

    @NotNull
    PluginDescriptionFile pluginDescription;

    public DependencyElement(@NotNull String pluginName, @NotNull Path pluginPath)
    {
        this.pluginName = pluginName;
        this.pluginPath = pluginPath;

        try
        {
            this.pluginDescription = PluginUtil.loadDescription(pluginPath.toFile());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


}
