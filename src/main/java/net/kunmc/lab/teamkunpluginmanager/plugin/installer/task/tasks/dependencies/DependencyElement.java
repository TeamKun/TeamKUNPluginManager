package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * 依存関係 系のタスクで使用される、概念的な依存関係を表すクラスです。
 */
@Value
@AllArgsConstructor
public class DependencyElement
{
    /**
     * 依存関係の名前です。
     */
    @Expose
    @NotNull
    String pluginName;
    /**
     * 依存関係プラグインがあるのパスです。
     */
    @Expose
    @NotNull
    Path pluginPath;

    /**
     * 依存関係のプラグイン情報ファイルです。
     */
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
