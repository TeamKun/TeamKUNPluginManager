package net.kunmc.lab.teamkunpluginmanager.utils.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
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
    private final int statusCode;

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
        return new HTTPResponse(status, request, null, null, -1, null, null);
    }

    /**
     * レスポンスボディを文字列として取得します。
     *
     * @return レスポンスボディ
     */
    public String getAsString()
    {
        if (inputStream == null)
            return null;
        else if (bodyCache != null)
            return bodyCache;

        StringBuilder sb = new StringBuilder();

        byte[] buffer = new byte[1024];
        int len;
        try
        {
            while ((len = inputStream.read(buffer)) != -1)
                sb.append(new String(buffer, 0, len));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        bodyCache = sb.toString();
        return bodyCache;
    }

    /**
     * レスポンスボディを JSON として取得します。
     *
     * @return レスポンスボディ
     */
    public JsonElement getAsJson()
    {
        if (inputStream == null)
            return null;

        String json = getAsString();
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
        if (inputStream == null)
            return null;

        String json = getAsString();
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
        if (inputStream != null)
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
        return headers.getOrDefault(header.toLowerCase(), headers.get(header));
    }

    /**
     * リクエストに成功したかどうかを取得します。
     *
     * @return リクエストに成功したかどうか
     */
    public boolean isSuccessful()
    {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * リダイレクトのレスポンスかどうかを取得します。
     *
     * @return リダイレクトのレスポンスかどうか
     */
    public boolean isRedirect()
    {
        return statusCode >= 300 && statusCode < 400;
    }

    /**
     * クライアントエラーのレスポンスかどうかを取得します。
     *
     * @return クライアントエラーのレスポンスかどうか
     */
    public boolean isClientError()
    {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * サーバエラーのレスポンスかどうかを取得します。
     *
     * @return サーバエラーのレスポンスかどうか
     */
    public boolean isServerError()
    {
        return statusCode >= 500 && statusCode < 600;
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
        return isClientError() || isServerError();
    }

    /**
     * OK のレスポンスかどうかを取得します。
     *
     * @return OK のレスポンスかどうか
     */
    public boolean isOK()
    {
        return statusCode == 200;
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
