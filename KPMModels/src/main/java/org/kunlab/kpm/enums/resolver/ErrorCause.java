package org.kunlab.kpm.enums.resolver;

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
     * サーバに接続するための資格情報が不正です。
     */
    INVALID_CREDENTIAL,
    /**
     * 不明なエラーが発生しました。
     */
    UNKNOWN_ERROR
}
