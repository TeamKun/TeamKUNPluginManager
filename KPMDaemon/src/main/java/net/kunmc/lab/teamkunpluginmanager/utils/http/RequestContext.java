package net.kunmc.lab.teamkunpluginmanager.utils.http;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;

/**
 * リクエストのコンテキストを表すクラスです。
 */
@Value
@Builder
public class RequestContext
{
    /**
     * URLです。
     */
    String url;

    /**
     * リクエストのメソッドです。
     * デフォルトはGETです。
     */
    @Builder.Default
    RequestMethod method = RequestMethod.GET;

    /**
     * 追加のリクエスト・ヘッダーです。
     */
    @Singular("header")
    Map<String, String> extraHeaders;

    /**
     * レスポンスがキャッシュ可能かどうかを表すフラグです。
     */
    @Builder.Default
    boolean cacheable = false;
    /**
     * リダイレクトに従うかどうかを表すフラグです。
     */
    @Builder.Default
    boolean followRedirects = true;
    /**
     * タイムアウト時間です。
     */
    @Builder.Default
    int timeout = -1;

    /**
     * リクエストボディです。
     */
    @Builder.Default
    byte[] body = null;

}
