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
    private final Source source; // TODO: フロントエンドに依存しないよう、メッセージプロバイダ作る。

    /**
     * この解決を提供したリゾルバです。
     */
    @Nullable
    private final BaseResolver resolver;

    public ErrorResult(@Nullable BaseResolver resolver, @NotNull ErrorCause cause, @NotNull Source source)
    {
        this.resolver = resolver;
        this.cause = cause;
        this.source = source;
    }

    /**
     * エラーのかんたんな理由を表す列挙型です。
     */
    public enum ErrorCause
    {
        /**
         * クエリに対応するリゾルバが見つかりませんでした。
         */
        RESOLVER_MISMATCH("対応するリゾルバが見つかりませんでした。"),
        /**
         * 不正なクエリが指定されました。
         */
        INVALID_QUERY("不正なクエリです。"),
        /**
         * プラグインが見つかりませんでした。
         */
        PLUGIN_NOT_FOUND("プラグインが見つかりませんでした。"),
        /**
         * この Minecraft サーバに適合するプラグインが見つかりませんでした。
         */
        MATCH_PLUGIN_NOT_FOUND("サーバに対応するプラグインが見つかりませんでした。"),
        /**
         * プラグインは見つかりましたが、 .jar や .zip などのファイルが見つかりませんでした。
         */
        ASSET_NOT_FOUND("プラグインは見つかりましたが、ファイルが見つかりませんでした。"),
        /**
         * サーバが不正なレスポンスを返しました。
         */
        SERVER_RESPONSE_MALFORMED("サーバが不正なレスポンスを返しました。"),
        /**
         * サーバがエラーを返しました。
         */
        SERVER_RESPONSE_ERROR("サーバがエラーを返答しました。"),
        /**
         * 不明なエラーが発生しました。
         */
        UNKNOWN_ERROR("不明なエラーが発生しました。");

        /**
         * エラーの詳細な理由です。
         */
        @Getter
        private String message;

        @Getter
        private boolean messageChanged;

        ErrorCause(String message)
        {
            this.message = message;
            this.messageChanged = false;
        }

        public ErrorCause value(String message)
        {
            this.message = message;
            this.messageChanged = true;
            return this;
        }

    }
}
