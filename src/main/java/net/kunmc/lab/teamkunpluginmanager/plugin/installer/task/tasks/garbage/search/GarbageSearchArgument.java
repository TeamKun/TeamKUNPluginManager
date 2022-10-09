package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.garbage.search;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.TaskArgument;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 不要データ検索を行うタスクの引数です。
 */
@Value
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class GarbageSearchArgument extends TaskArgument
{
    /**
     * 検索から除外するデータのリストです。
     */
    @NotNull
    List<String> excludes;

    /**
     * 検索するディレクトリのパスです。
     */
    @NotNull
    Path dataFolder;

    /**
     * サーバに存在するプラグインのリストです。
     */
    @NotNull
    List<String> plugins;

    public GarbageSearchArgument(List<String> excludes)
    {
        this(excludes, TeamKunPluginManager.getPlugin().getDataFolder().toPath().getParent(),
                Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                        .map(Plugin::getName)
                        .collect(Collectors.toList())
        );
    }
}
