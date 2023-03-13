package org.kunlab.kpm;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.KPMEnvironment;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * KPM の実行環境を表すクラスです。
 */
@Getter
@Builder
public class KPMEnvironmentImpl implements KPMEnvironment
{
    @NotNull
    private final Plugin plugin;

    @NotNull
    private final Path dataDirPath;

    @NotNull
    private final Logger logger;

    @NotNull
    private final Path tokenPath;

    @NotNull
    private final Path tokenKeyPath;

    @NotNull
    private final Path metadataDBPath;

    @NotNull
    private final Path aliasesDBPath;

    @NotNull
    @Singular("organization")
    private final List<String> organizations;

    @NotNull
    @Singular("excludePlugin")
    private final List<String> excludes;

    @NotNull
    @Singular("sources")
    private final Map<String, String> sources;

    private final String HTTPUserAgent;
    private final int HTTPTimeout;
    private final int HTTPMaxRedirects;

    @NotNull
    private final ExceptionHandler exceptionHandler;

    public static KPMEnvironmentImplBuilder builder(@NotNull Plugin plugin, @NotNull Logger logger, @NotNull Path dataDirPath)
    {
        return new KPMEnvironmentImplBuilder()
                .plugin(plugin)
                .logger(logger)
                .dataDirPath(dataDirPath)
                .excludePlugin("TeamKunPluginManager")
                .excludePlugin("bStats")
                .exceptionHandler(new BasicExceptionHandler(logger));
    }

    public static KPMEnvironmentImplBuilder builder(@NotNull Plugin plugin)
    {
        return builder(plugin, plugin.getLogger(), plugin.getDataFolder().toPath());
    }
}
