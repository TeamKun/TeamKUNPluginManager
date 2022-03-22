package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.BaseResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * プラグイン解決中のエラーの結果。
 */
@EqualsAndHashCode
@Getter

public class ErrorResult implements ResolveResult
{
    /**
     * エラーケース
     */
    @NotNull
    private final ErrorCause cause;

    /**
     * プラグインの供給元
     */
    @NotNull
    private final Source source; // TODO: フロントエンドに依存しないよう、メッセージプロバイダ作る。

    /**
     * この結果を提供したリゾルバ
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
     * エラーケース
     */
    public enum ErrorCause
    {
        /**
         * リゾルバが合っていない。
         */
        RESOLVER_MISMATCH("対応するリゾルバが見つかりませんでした。"),
        /**
         * 不正なクエリ
         */
        INVALID_QUERY("不正なクエリです。"),
        /**
         * プラグインがみつからない。
         * サーバが404を返した場合もこのケースになる。
         */
        PLUGIN_NOT_FOUND("プラグインが見つかりませんでした。"),
        /**
         * サーバに合ったプラグインが見つからなかった
         */
        MATCH_PLUGIN_NOT_FOUND("サーバに対応するプラグインが見つかりませんでした。"),
        /**
         * プラグイン自体は見つかったが、アセット(jarファイル)が見つからない。
         */
        ASSET_NOT_FOUND("プラグインは見つかりましたが、ファイルが見つかりませんでした。"),
        /**
         * サーバのレスポンスが破損している/おかしい。
         */
        SERVER_RESPONSE_MALFORMED("サーバが不正なレスポンスを返しました。"),
        /**
         * その他のサーバに関するエラー
         */
        SERVER_RESPONSE_ERROR("サーバがエラーを返答しました。"),
        /**
         * その他のエラー
         */
        UNKNOWN_ERROR("不明なエラーが発生しました。");

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
