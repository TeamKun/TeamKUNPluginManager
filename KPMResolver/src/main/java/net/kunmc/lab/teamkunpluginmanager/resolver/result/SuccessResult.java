package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;
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

    /**
     * この結果を提供したリゾルバ
     */
    @NotNull
    private final BaseResolver resolver;

    public SuccessResult(BaseResolver resolver, @NotNull String downloadUrl, @NotNull Source source)
    {
        this(resolver, downloadUrl, null, null, source);
    }

    public SuccessResult(@NotNull BaseResolver resolver, @NotNull String downloadUrl, @Nullable String fileName, @Nullable String version, @NotNull Source source)
    {
        this.resolver = resolver;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.version = version;
        this.source = source;
    }
}
