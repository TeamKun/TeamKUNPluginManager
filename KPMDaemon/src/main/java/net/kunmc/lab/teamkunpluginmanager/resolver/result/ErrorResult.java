package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 解決に失敗したことを表すクエリ解決結果です。
 */
@EqualsAndHashCode
@Getter

public class ErrorResult implements ResolveResult
{
    /**
     * エラーのかんたんな理由です。
     */
    @NotNull
    private final ErrorCause cause;

    /**
     * プラグインの提供元です。
     */
    @NotNull
    private final Source source;

    /**
     * この解決を提供したリゾルバです。
     */
    @Nullable
    private final BaseResolver resolver;

    /**
     * エラーの詳細な理由です。
     */
    @Nullable
    private final String message;

    public ErrorResult(@Nullable BaseResolver resolver, @NotNull ErrorCause cause, @NotNull Source source, @Nullable String message)
    {
        this.cause = cause;
        this.source = source;
        this.resolver = resolver;
        this.message = message;
    }

    public ErrorResult(@Nullable BaseResolver resolver, @NotNull ErrorCause cause, @NotNull Source source)
    {
        this(resolver, cause, source, null);
    }

    /**
     * エラーのかんたんな理由を表す列挙型です。
     */
    public enum ErrorCause
    {
        /**
         * クエリに対応するリゾルバが見つかりませんでした。
         */
        RESOLVER_MISMATCH,
        /**
         * 不正なクエリが指定されました。
         */
        INVALID_QUERY,
        /**
         * ホストの解決に失敗しました。
         */
        HOST_RESOLVE_FAILED,
        /**
         * プラグインが見つかりませんでした。
         */
        PLUGIN_NOT_FOUND,
        /**
         * この Minecraft サーバに適合するプラグインが見つかりませんでした。
         */
        VERSION_MISMATCH,
        /**
         * プラグインは見つかりましたが、 .jar や .zip などのファイルが見つかりませんでした。
         */
        ASSET_NOT_FOUND,
        /**
         * サーバが不正なレスポンスを返しました。
         */
        SERVER_RESPONSE_MALFORMED,
        /**
         * サーバがエラーを返しました。
         */
        SERVER_RESPONSE_ERROR,
        /**
         * 不明なエラーが発生しました。
         */
        UNKNOWN_ERROR
    }
}
