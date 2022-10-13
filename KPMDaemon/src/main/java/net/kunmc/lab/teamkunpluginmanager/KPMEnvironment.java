package net.kunmc.lab.teamkunpluginmanager;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * KPM の実行環境を表すクラスです。
 */
@Builder
@Getter
public class KPMEnvironment
{
    /**
     * KPMのプラグインです。
     */
    @NotNull
    private final Plugin plugin;

    /**
     * プラグインのデータディレクトリのパスです。
     */
    @NotNull
    private final Path dataDirPath;

    /**
     * KPMデーモンが使用するロガーです。
     */
    @NotNull
    private final Logger logger;

    /**
     * トークンの格納先のパスです。
     */
    @NotNull
    private final Path tokenPath;

    /**
     * トークンの鍵の格納先のパスです。
     */
    @NotNull
    private final Path tokenKeyPath;

    /**
     * プラグインメタデータデータベースのパスです。
     */
    @NotNull
    private final Path metadataDBPath;

    /**
     * エイリアスデータベースのパスです。
     */
    @NotNull
    private final Path aliasesDBPath;

    /**
     * プラグイン解決に使用するGitHubの組織名です。
     */
    @NotNull
    @Singular("organization")
    private final List<String> organizations;

    /**
     * 様々な操作から除外するプラグインの名前です。
     * 通常は、削除やアップデートを行わないようにするために使用します。
     */
    @NotNull
    @Singular("excludePlugin")
    private final List<String> excludes;

    /**
     * エイリアスのソースです。
     */
    @NotNull
    @Singular("sources")
    private final Map<String, String> sources;

    public static KPMEnvironmentBuilder builder(@NotNull Plugin plugin, @NotNull Logger logger, @NotNull Path dataDirPath)
    {
        return new KPMEnvironmentBuilder()
                .plugin(plugin)
                .logger(logger)
                .dataDirPath(dataDirPath);
    }

    public static KPMEnvironmentBuilder builder(@NotNull Plugin plugin)
    {
        return builder(plugin, plugin.getSLF4JLogger(), plugin.getDataFolder().toPath());
    }
}
