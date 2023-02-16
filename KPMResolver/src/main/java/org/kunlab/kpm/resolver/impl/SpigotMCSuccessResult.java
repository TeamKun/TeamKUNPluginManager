package org.kunlab.kpm.resolver.impl;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.resolver.BaseResolver;
import org.kunlab.kpm.interfaces.resolver.result.MarketplaceResult;
import org.kunlab.kpm.interfaces.resolver.result.VersionedResult;
import org.kunlab.kpm.resolver.result.AbstractSuccessResult;

import java.util.List;

@Getter
public class SpigotMCSuccessResult extends AbstractSuccessResult implements MarketplaceResult, VersionedResult
{
    @NotNull
    private final String title;

    @NotNull
    private final String url;

    @NotNull
    private final String description;

    @NotNull
    private final List<String> versions;

    public SpigotMCSuccessResult(@NotNull BaseResolver resolver, @Nullable String version, @NotNull String title, long id, @NotNull String description, @NotNull List<String> versions)
    {
        super(resolver, "https://apple.api.spiget.org/v2/resources/" + id +
                        (version != null ? "/versions/" + version: "") + "/download",
                null, version, Source.SPIGOT_MC
        );
        this.title = title;
        this.description = description;
        this.url = "https://www.spigotmc.org/resources/" + id;
        this.versions = versions;

    }
}
