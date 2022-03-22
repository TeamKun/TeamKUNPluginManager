package net.kunmc.lab.teamkunpluginmanager.resolver.impl;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MarketplaceResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * CurseForge または Bukkit.org の成功結果を表すクラス
 */
@Getter
public class CurseBukkitSuccessResult extends SuccessResult implements MarketplaceResult
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

    @Override
    public String getUrl()
    {
        if (this.getSource() == Source.CURSE_FORGE)
            return "https://www.curseforge.com/minecraft/bukkit-plugins/" + this.slug;
        else
            return "https://dev.bukkit.org/projects/" + this.slug;
    }


    @Override
    public String getDescription()
    {
        if (this.description != null)
            return this.description;

        Pair<Integer, String> description =
                URLUtils.getAsString("https://addons-ecs.forgesvc.net/api/v2/addon/" + this.id +"/description");

        if (description.getLeft() != 200)
            return "Failed to get description";

        this.description = description.getRight();
        return this.description;
    }


}
