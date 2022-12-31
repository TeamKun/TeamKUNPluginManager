package net.kunmc.lab.kpm.resolver.result;

import lombok.Data;
import net.kunmc.lab.kpm.interfaces.resolver.BaseResolver;
import net.kunmc.lab.kpm.interfaces.resolver.result.SuccessResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 解決に成功したことを表すクエリ解決結果です。
 */
@Data
public abstract class AbstractSuccessResult implements SuccessResult
{
    @NotNull
    private final String downloadUrl;

    @Nullable
    private final String fileName;

    @Nullable
    private final String version;

    @NotNull
    private final Source source;

    @NotNull
    private final BaseResolver resolver;

    public AbstractSuccessResult(BaseResolver resolver, @NotNull String downloadUrl, @NotNull Source source)
    {
        this(resolver, downloadUrl, null, null, source);
    }

    public AbstractSuccessResult(@NotNull BaseResolver resolver, @NotNull String downloadUrl, @Nullable String fileName, @Nullable String version, @NotNull Source source)
    {
        this.resolver = resolver;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.version = version;
        this.source = source;
    }
}
