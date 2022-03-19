package net.kunmc.lab.teamkunpluginmanager.resolver.result;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

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

    public ErrorResult(@NotNull ErrorCause cause, @NotNull Source source)
    {
        this.cause = cause;
        this.source = source;
    }
    /**
     * エラーケース
     */
    @AllArgsConstructor
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
        MATCH_PLUGIN_NOT_FOUND("対応するプラグインが見つかりませんでした。"),
        /**
         * プラグイン自体は見つかったが、アセット(jarファイル)が見つからない。
         */
        ASSET_NOT_FOUND("アセットが見つかりませんでした。"),
        /**
         * サーバのレスポンスが破損している/おかしい。
         */
        SERVER_RESPONSE_MALFORMED("サーバが不正なレスポンスを返しました。"),
        /**
         * その他のサーバに関するエラー
         */
        SERVER_RESPONSE_ERROR("サーバがエラーを返しました。"),
        /**
         * その他のエラー
         */
        UNKNOWN_ERROR("不明なエラーが発生しました。");

        @Getter
        private String message;

        public ErrorCause value(String message)
        {
            this.message = message;
            return this;
        }

    }
}
