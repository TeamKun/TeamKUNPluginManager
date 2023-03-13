package org.kunlab.kpm.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;

/**
 * HTTP レスポンスを表すクラスです。
 */
@Data
public class HTTPResponse implements AutoCloseable
{
    /**
     * リクエストのステータスです。
     */
    private final RequestStatus status;
    /**
     * リクエストのコンテキストです。
     */
    private final RequestContext request;

    /**
     * サーバのプロトコルです。
     */
    private final String serverProtocol;
    /**
     * プロトコルのバージョンです。
     */
    private final String protocolVersion;

    /**
     * HTTP ステータスコードです。
     */
    private final StatusCode statusCode;

    /**
     * HTTP ヘッダーです。
     */
    private final HashMap<String, String> headers;

    /**
     * レスポンスボディを表すストリームです。
     */
    @Nullable
    private final InputStream inputStream;

    @Getter(AccessLevel.PRIVATE)
    private String bodyCache = null;

    /**
     * エラーのレスポンスを生成します。
     *
     * @param request リクエスト
     * @param status  ステータス
     * @return エラーのレスポンス
     */
    public static HTTPResponse error(@NotNull RequestContext request, @NotNull RequestStatus status)
    {
        return new HTTPResponse(status, request, null, null,
                StatusCode.UNKNOWN, null, null
        );
    }

    /**
     * レスポンスボディを文字列として取得します。
     *
     * @return レスポンスボディ
     */
    public String getAsString()
    {
        if (this.inputStream == null)
            return null;
        else if (this.bodyCache != null)
            return this.bodyCache;

        StringBuilder sb = new StringBuilder();

        byte[] buffer = new byte[Requests.HTTP_BUFFER_SIZE];
        int len;
        try
        {
            while ((len = this.inputStream.read(buffer)) != -1)
                sb.append(new String(buffer, 0, len));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }

        this.bodyCache = sb.toString();
        return this.bodyCache;
    }

    /**
     * レスポンスボディを JSON として取得します。
     *
     * @return レスポンスボディ
     */
    public JsonElement getAsJson()
    {
        if (this.inputStream == null)
            return null;

        String json = this.getAsString();
        return new Gson().fromJson(json, JsonElement.class);
    }

    /**
     * レスポンスボディをオブジェクトとして取得します。
     *
     * @param clazz クラス
     * @param <T>   クラスの型
     * @return レスポンスボディ
     */
    public <T> T getAsObject(Class<T> clazz)
    {
        if (this.inputStream == null)
            return null;

        String json = this.getAsString();
        return new Gson().fromJson(json, clazz);
    }

    /**
     * このクラスを破棄します。
     *
     * @throws IOException I/O エラーが発生した場合
     */
    @Override
    public void close() throws IOException
    {
        if (this.inputStream != null)
            this.inputStream.close();
    }

    /**
     * ヘッダーを取得します。
     * ヘッダー名は大文字小文字を区別しません。
     *
     * @param header ヘッダー名
     * @return ヘッダーの値
     */
    @Nullable
    public String getHeader(@NotNull String header)
    {
        return this.headers.getOrDefault(header.toLowerCase(), this.headers.get(header));
    }

    /**
     * リクエストに成功したかどうかを取得します。
     *
     * @return リクエストに成功したかどうか
     */
    public boolean isSuccessful()
    {
        return this.statusCode.isSuccess();
    }

    /**
     * リダイレクトのレスポンスかどうかを取得します。
     *
     * @return リダイレクトのレスポンスかどうか
     */
    public boolean isRedirect()
    {
        return this.statusCode.isRedirect();
    }

    /**
     * クライアントエラーのレスポンスかどうかを取得します。
     *
     * @return クライアントエラーのレスポンスかどうか
     */
    public boolean isClientError()
    {
        return this.statusCode.isClientError();
    }

    /**
     * サーバエラーのレスポンスかどうかを取得します。
     *
     * @return サーバエラーのレスポンスかどうか
     */
    public boolean isServerError()
    {
        return this.statusCode.isServerError();
    }

    /**
     * エラーのレスポンスかどうかを取得します。
     *
     * @return エラーのレスポンスかどうか
     * @see #isClientError()
     * @see #isServerError()
     */
    public boolean isError()
    {
        return this.isClientError() || this.isServerError();
    }

    /**
     * OK のレスポンスかどうかを取得します。
     *
     * @return OK のレスポンスかどうか
     */
    public boolean isOK()
    {
        return this.statusCode == StatusCode.OK;
    }

    /**
     * リクエストのステータスを表す列挙型です。
     */
    public enum RequestStatus
    {
        /**
         * リクエストが成功しました。
         */
        OK,
        /**
         * サーバでエラーが発生しました。
         */
        SERVER_ERROR,
        /**
         * クライアントの問題でエラーが発生しました。
         */
        CLIENT_ERROR,

        /**
         * リダイレクト先に指定された URL が無効でした。
         */
        REDIRECT_LOCATION_MALFORMED,
        /**
         * リダイレクト上限に達しました。
         */
        REDIRECT_LIMIT_EXCEED,
        /**
         * ホストが見つかりませんでした。
         */
        UNABLE_TO_RESOLVE_HOST,
        /**
         * I/O エラーが発生しました。
         */
        IO_EXCEPTION_OCCURRED,
        /**
         * URL が無効でした。
         */
        URL_MALFORMED
    }
}
