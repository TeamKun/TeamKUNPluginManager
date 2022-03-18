package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 解決成功のResolveResult実装。
 */
@Data
public class SuccessResult implements ResolveResult
{
    /**
     * プラグインのダウンロードリンク
     */
    @NotNull
    private final String downloadUrl;

    /**
     * プラグインのファイル名
     */
    @Nullable
    private final String fileName;

    /**
     * プラグインのバージョン
     */
    @Nullable
    private final String version;

    /**
     * プラグインの供給元
     */
    @NotNull
    private final Source source;

    public SuccessResult(@NotNull String downloadUrl, @NotNull Source source)
    {
        this(downloadUrl, null, null, source);
    }

    public SuccessResult(@NotNull String downloadUrl, @Nullable String fileName, @Nullable String version, @NotNull Source source)
    {
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.version = version;
        this.source = source;
    }
}
