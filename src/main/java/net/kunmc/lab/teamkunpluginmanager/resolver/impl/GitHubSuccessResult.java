package net.kunmc.lab.teamkunpluginmanager.resolver.impl;

import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MarketplaceResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class GitHubSuccessResult extends SuccessResult implements MarketplaceResult
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

    public GitHubSuccessResult(@NotNull String downloadUrl, @Nullable String fileName, @Nullable String version, @NotNull String repoName, @NotNull String htmlPage, @NotNull String owner, long size, @NotNull String releaseName, @NotNull String releaseBody, long releaseId)
    {
        super(downloadUrl, fileName, version, Source.GITHUB);
        this.owner = owner;
        this.size = size;
        this.releaseId = releaseId;

        this.releaseName = releaseName;
        this.releaseBody = releaseBody;
        this.repoName = repoName;
    }

    @Override
    public String getTitle()
    {
        return repoName + " - " + releaseName;
    }

    @Override
    public String getUrl()
    {
        return "https://github.com/" + owner + "/" + repoName;
    }

    @Override
    public String getDescription()
    {
        return releaseBody;
    }
}