package org.kunlab.kpm.resolver.impl;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.resolver.BaseResolver;
import org.kunlab.kpm.interfaces.resolver.result.MarketplaceResult;
import org.kunlab.kpm.resolver.result.AbstractSuccessResult;

import javax.annotation.Nonnull;

@Getter
public class GitHubSuccessResult extends AbstractSuccessResult implements MarketplaceResult
{
    /**
     * リポジトリ(プラグイン)のオーナー
     */
    @NotNull
    private final String owner;

    /**
     * ファイルサイズ
     */
    private final long size;

    /**
     * リリースの名前
     */
    @NotNull
    private final String releaseName;

    /**
     * リリースの内容
     */
    @NotNull
    private final String releaseBody;

    /**
     * リポジトリの名前
     */
    @NotNull
    private final String repoName;

    /**
     * リリースのid
     */
    private final long releaseId;

    /**
     * プレリリースかどうか
     */
    private final boolean isPreRelease;

    public GitHubSuccessResult(@NotNull BaseResolver resolver, @NotNull String downloadUrl, @Nullable String fileName, @Nullable String version, @NotNull String repoName, @NotNull String owner, long size, @NotNull String releaseName, @NotNull String releaseBody, long releaseId, boolean isPreRelease)
    {
        super(resolver, downloadUrl, fileName, version, Source.GITHUB);
        this.owner = owner;
        this.size = size;
        this.releaseId = releaseId;

        this.releaseName = releaseName;
        this.releaseBody = releaseBody;
        this.repoName = repoName;
        this.isPreRelease = isPreRelease;
    }

    @Nonnull
    @Override
    public String getTitle()
    {
        return this.repoName + " - " + this.releaseName;
    }

    @Nonnull
    @Override
    public String getUrl()
    {
        return "https://github.com/" + this.owner + "/" + this.repoName;
    }

    @Nonnull
    @Override
    public String getDescription()
    {
        return this.releaseBody;
    }
}
