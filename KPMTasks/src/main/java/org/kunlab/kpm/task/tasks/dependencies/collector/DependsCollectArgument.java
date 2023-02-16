package org.kunlab.kpm.task.tasks.dependencies.collector;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.task.TaskArgument;
import org.kunlab.kpm.resolver.QueryContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 依存関係の取得に必要な引数を表します。
 *
 * @see DependsCollectTask
 */
@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DependsCollectArgument implements TaskArgument
{
    /**
     * 依存関係を取得する対象のプラグインのプラグイン情報ファイルです。
     */
    @NotNull
    PluginDescriptionFile pluginDescription;
    /**
     * 依存関係のソースです。全ての依存関係に必須なものではありません。
     */
    @NotNull
    Map<String, QueryContext> sources;
    /**
     * 既にサーバにインストールされているまたは既に取得済みの依存関係です。
     */
    @NotNull
    List<String> alreadyInstalledPlugins;

    /**
     * プラグイン情報ファイルから依存関係を取得します。
     * {@link #alreadyInstalledPlugins} には、サーバにインストールされているプラグインが自動で追加されます。
     *
     * @param pluginDescription プラグイン情報ファイル
     * @see org.bukkit.plugin.PluginManager#getPlugins()
     */
    public DependsCollectArgument(@NotNull PluginDescriptionFile pluginDescription, @NotNull Map<String, QueryContext> sources)
    {
        this(pluginDescription, sources,
                Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                        .map(plugin -> plugin.getDescription().getName())
                        .collect(Collectors.toList())
        );
    }
}
