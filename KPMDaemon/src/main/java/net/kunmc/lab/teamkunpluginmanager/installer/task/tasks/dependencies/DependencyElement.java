package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.dependencies;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
    String pluginName;
    /**
     * 依存関係プラグインがあるのパスです。
     */
    @NotNull
    Path pluginPath;

    /**
     * 依存関係のプラグイン情報ファイルです。
     */
    @NotNull
    PluginDescriptionFile pluginDescription;

    /**
     * 依存関係の解決に使用したクエリです。
     */
    @Nullable
    String query;

    public DependencyElement(@NotNull String pluginName, @NotNull Path pluginPath, @NotNull String query)
    {
        this.pluginName = pluginName;
        this.pluginPath = pluginPath;
        this.query = query;

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
