package org.kunlab.kpm.resolver.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.http.HTTPResponse;
import org.kunlab.kpm.http.RequestContext;
import org.kunlab.kpm.http.Requests;
import org.kunlab.kpm.resolver.interfaces.result.MarketplaceResult;
import org.kunlab.kpm.resolver.result.AbstractSuccessResult;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;

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
                .url("https://api.curse.tools/v1/cf/mods/" + this.id + "/description")
                .build()))
        {
            if (response.isError())
                return this.description = "Failed to get description";

            JsonElement element;
            JsonObject responseObj;
            if ((element = response.getAsJson()).isJsonObject())
                responseObj = element.getAsJsonObject();
            else
                return this.description = "Failed to parse description";

            return this.description = responseObj.get("data").getAsString();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }


}
