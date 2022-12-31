package net.kunmc.lab.kpm.task.tasks.dependencies.collector;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.interfaces.task.TaskArgument;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 依存関係の取得に必要な引数を表します。
 *
 * @see DependsCollectTask
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class DependsCollectArgument implements TaskArgument
{
    /**
     * 依存関係を取得する対象のプラグインのプラグイン情報ファイルです。
     */
    @NotNull
    PluginDescriptionFile pluginDescription;
    /**
     * 既にサーバにインストールされているまたは既に取得済みの依存関係です。
     */
    @NotNull
    List<String> alreadyInstalledPlugins;

    public DependsCollectArgument(@NotNull PluginDescriptionFile pluginDescription,
                                  @NotNull List<String> alreadyInstalledPlugins)
    {
        this.pluginDescription = pluginDescription;
        this.alreadyInstalledPlugins = alreadyInstalledPlugins;
    }

    /**
     * プラグイン情報ファイルから依存関係を取得します。
     * {@link #alreadyInstalledPlugins} には、サーバにインストールされているプラグインが自動で追加されます。
     *
     * @param pluginDescription プラグイン情報ファイル
     * @see org.bukkit.plugin.PluginManager#getPlugins()
     */
    public DependsCollectArgument(@NotNull PluginDescriptionFile pluginDescription)
    {
        this(pluginDescription, Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                .map(plugin -> plugin.getDescription().getName())
                .collect(Collectors.toList()));
    }
}
