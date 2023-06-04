package org.kunlab.kpm;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.KPMEnvironment;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * KPM の実行環境を表すクラスです。
 */
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
    private final List<String> organizations;

    @NotNull
    private final List<String> excludes;

    @NotNull
    private final Map<String, String> sources;

    private final String HTTPUserAgent;
    private final int HTTPTimeout;
    private final int HTTPMaxRedirects;

    @NotNull
    private final ExceptionHandler exceptionHandler;

    KPMEnvironmentImpl(@NotNull Plugin plugin, @NotNull Path dataDirPath, @NotNull Logger logger, @NotNull Path tokenPath, @NotNull Path tokenKeyPath, @NotNull Path metadataDBPath, @NotNull Path aliasesDBPath, @NotNull List<String> organizations, @NotNull List<String> excludes, @NotNull Map<String, String> sources, String HTTPUserAgent, int HTTPTimeout, int HTTPMaxRedirects, @NotNull ExceptionHandler exceptionHandler)
    {
        this.plugin = plugin;
        this.dataDirPath = dataDirPath;
        this.logger = logger;
        this.tokenPath = tokenPath;
        this.tokenKeyPath = tokenKeyPath;
        this.metadataDBPath = metadataDBPath;
        this.aliasesDBPath = aliasesDBPath;
        this.organizations = organizations;
        this.excludes = excludes;
        this.sources = sources;
        this.HTTPUserAgent = HTTPUserAgent;
        this.HTTPTimeout = HTTPTimeout;
        this.HTTPMaxRedirects = HTTPMaxRedirects;
        this.exceptionHandler = exceptionHandler;
    }

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

    public @NotNull Plugin getPlugin()
    {
        return this.plugin;
    }

    public @NotNull Path getDataDirPath()
    {
        return this.dataDirPath;
    }

    public @NotNull Logger getLogger()
    {
        return this.logger;
    }

    public @NotNull Path getTokenPath()
    {
        return this.tokenPath;
    }

    public @NotNull Path getTokenKeyPath()
    {
        return this.tokenKeyPath;
    }

    public @NotNull Path getMetadataDBPath()
    {
        return this.metadataDBPath;
    }

    public @NotNull Path getAliasesDBPath()
    {
        return this.aliasesDBPath;
    }

    public @NotNull List<String> getOrganizations()
    {
        return this.organizations;
    }

    public @NotNull List<String> getExcludes()
    {
        return this.excludes;
    }

    public @NotNull Map<String, String> getSources()
    {
        return this.sources;
    }

    public String getHTTPUserAgent()
    {
        return this.HTTPUserAgent;
    }

    public int getHTTPTimeout()
    {
        return this.HTTPTimeout;
    }

    public int getHTTPMaxRedirects()
    {
        return this.HTTPMaxRedirects;
    }

    public @NotNull ExceptionHandler getExceptionHandler()
    {
        return this.exceptionHandler;
    }

    public static class KPMEnvironmentImplBuilder
    {
        private Plugin plugin;
        private Path dataDirPath;
        private Logger logger;
        private Path tokenPath;
        private Path tokenKeyPath;
        private Path metadataDBPath;
        private Path aliasesDBPath;
        private ArrayList<String> organizations;
        private ArrayList<String> excludes;
        private ArrayList<String> sources$key;
        private ArrayList<String> sources$value;
        private String HTTPUserAgent;
        private int HTTPTimeout;
        private int HTTPMaxRedirects;
        private ExceptionHandler exceptionHandler;

        public KPMEnvironmentImplBuilder plugin(@NotNull Plugin plugin)
        {
            this.plugin = plugin;
            return this;
        }

        public KPMEnvironmentImplBuilder dataDirPath(@NotNull Path dataDirPath)
        {
            this.dataDirPath = dataDirPath;
            return this;
        }

        public KPMEnvironmentImplBuilder logger(@NotNull Logger logger)
        {
            this.logger = logger;
            return this;
        }

        public KPMEnvironmentImplBuilder tokenPath(@NotNull Path tokenPath)
        {
            this.tokenPath = tokenPath;
            return this;
        }

        public KPMEnvironmentImplBuilder tokenKeyPath(@NotNull Path tokenKeyPath)
        {
            this.tokenKeyPath = tokenKeyPath;
            return this;
        }

        public KPMEnvironmentImplBuilder metadataDBPath(@NotNull Path metadataDBPath)
        {
            this.metadataDBPath = metadataDBPath;
            return this;
        }

        public KPMEnvironmentImplBuilder aliasesDBPath(@NotNull Path aliasesDBPath)
        {
            this.aliasesDBPath = aliasesDBPath;
            return this;
        }

        public KPMEnvironmentImplBuilder organizations(Collection<? extends String> organizations)
        {
            if (organizations == null)
                throw new NullPointerException("organizations cannot be null");
            if (this.organizations == null) this.organizations = new ArrayList<String>();
            this.organizations.addAll(organizations);
            return this;
        }

        public KPMEnvironmentImplBuilder excludePlugin(String excludePlugin)
        {
            if (this.excludes == null) this.excludes = new ArrayList<String>();
            this.excludes.add(excludePlugin);
            return this;
        }

        public KPMEnvironmentImplBuilder excludes(Collection<? extends String> excludes)
        {
            if (excludes == null)
                throw new NullPointerException("excludes cannot be null");
            if (this.excludes == null) this.excludes = new ArrayList<String>();
            this.excludes.addAll(excludes);
            return this;
        }

        public KPMEnvironmentImplBuilder sources(Map<? extends String, ? extends String> sources)
        {
            if (sources == null)
                throw new NullPointerException("sources cannot be null");

            if (this.sources$key == null)
            {
                this.sources$key = new ArrayList<>();
                this.sources$value = new ArrayList<>();
            }

            for (final Map.Entry<? extends String, ? extends String> $lombokEntry : sources.entrySet())
            {
                this.sources$key.add($lombokEntry.getKey());
                this.sources$value.add($lombokEntry.getValue());
            }
            return this;
        }

        public KPMEnvironmentImplBuilder HTTPUserAgent(String HTTPUserAgent)
        {
            this.HTTPUserAgent = HTTPUserAgent;
            return this;
        }

        public KPMEnvironmentImplBuilder HTTPTimeout(int HTTPTimeout)
        {
            this.HTTPTimeout = HTTPTimeout;
            return this;
        }

        public KPMEnvironmentImplBuilder HTTPMaxRedirects(int HTTPMaxRedirects)
        {
            this.HTTPMaxRedirects = HTTPMaxRedirects;
            return this;
        }

        public KPMEnvironmentImplBuilder exceptionHandler(@NotNull ExceptionHandler exceptionHandler)
        {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public KPMEnvironmentImpl build()
        {
            List<String> organizations;
            switch (this.organizations == null ? 0: this.organizations.size())
            {
                case 0:
                    organizations = Collections.emptyList();
                    break;
                case 1:
                    organizations = Collections.singletonList(this.organizations.get(0));
                    break;
                default:
                    organizations = Collections.unmodifiableList(new ArrayList<>(this.organizations));
            }
            List<String> excludes;
            switch (this.excludes == null ? 0: this.excludes.size())
            {
                case 0:
                    excludes = Collections.emptyList();
                    break;
                case 1:
                    excludes = Collections.singletonList(this.excludes.get(0));
                    break;
                default:
                    excludes = Collections.unmodifiableList(new ArrayList<String>(this.excludes));
            }
            Map<String, String> sources;
            switch (this.sources$key == null ? 0: this.sources$key.size())
            {
                case 0:
                    sources = Collections.emptyMap();
                    break;
                case 1:
                    sources = Collections.singletonMap(this.sources$key.get(0), this.sources$value.get(0));
                    break;
                default:
                    sources = new LinkedHashMap<>();
                    for (int $i = 0; $i < this.sources$key.size(); $i++)
                        sources.put(this.sources$key.get($i), this.sources$value.get($i));
                    sources = Collections.unmodifiableMap(sources);
            }

            return new KPMEnvironmentImpl(this.plugin, this.dataDirPath, this.logger, this.tokenPath, this.tokenKeyPath, this.metadataDBPath, this.aliasesDBPath, organizations, excludes, sources, this.HTTPUserAgent, this.HTTPTimeout, this.HTTPMaxRedirects, this.exceptionHandler);
        }

        public String toString()
        {
            return "KPMEnvironmentImpl.KPMEnvironmentImplBuilder(plugin=" + this.plugin + ", dataDirPath=" + this.dataDirPath + ", logger=" + this.logger + ", tokenPath=" + this.tokenPath + ", tokenKeyPath=" + this.tokenKeyPath + ", metadataDBPath=" + this.metadataDBPath + ", aliasesDBPath=" + this.aliasesDBPath + ", organizations=" + this.organizations + ", excludes=" + this.excludes + ", sources$key=" + this.sources$key + ", sources$value=" + this.sources$value + ", HTTPUserAgent=" + this.HTTPUserAgent + ", HTTPTimeout=" + this.HTTPTimeout + ", HTTPMaxRedirects=" + this.HTTPMaxRedirects + ", exceptionHandler=" + this.exceptionHandler + ")";
        }
    }
}
