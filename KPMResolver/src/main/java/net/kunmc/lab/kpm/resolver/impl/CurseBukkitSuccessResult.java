package net.kunmc.lab.kpm.resolver.impl;

import lombok.Getter;
import net.kunmc.lab.kpm.http.HTTPResponse;
import net.kunmc.lab.kpm.http.RequestContext;
import net.kunmc.lab.kpm.http.Requests;
import net.kunmc.lab.kpm.interfaces.resolver.result.MarketplaceResult;
import net.kunmc.lab.kpm.resolver.result.AbstractSuccessResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * CurseForge または Bukkit.org の成功結果を表すクラス
 */
@Getter
public class CurseBukkitSuccessResult extends AbstractSuccessResult implements MarketplaceResult
{
    @NotNull
    private final String title;

    @NotNull
    private final String slug;

    private final long id;

    private String description = null;

    public CurseBukkitSuccessResult(@NotNull CurseBukkitResolver resolver, @NotNull String downloadUrl, long id, @Nullable String fileName, @Nullable String version, @NotNull Source source, @NotNull String slug, @NotNull String title)
    {
        super(resolver, downloadUrl, fileName, version, source);

        if (source != Source.CURSE_FORGE && source != Source.DEV_BUKKIT)
            throw new IllegalArgumentException("source must be CurseForge or DevBukkit");

        this.id = id;
        this.slug = slug;
        this.title = title;
    }

    @Nonnull
    @Override
    public String getUrl()
    {
        if (this.getSource() == Source.CURSE_FORGE)
            return "https://www.curseforge.com/minecraft/bukkit-plugins/" + this.slug;
        else
            return "https://dev.bukkit.org/projects/" + this.slug;
    }

    @Nonnull
    @Override
    public String getDescription()
    {
        if (this.description != null)
            return this.description;

        try (HTTPResponse response = Requests.request(RequestContext.builder()
                .url("https://addons-ecs.forgesvc.net/api/v2/addon/" + this.id + "/description")
                .build()))
        {
            if (response.isError())
                return "Failed to get description";

            this.description = response.getAsString();
            return this.description;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


}
