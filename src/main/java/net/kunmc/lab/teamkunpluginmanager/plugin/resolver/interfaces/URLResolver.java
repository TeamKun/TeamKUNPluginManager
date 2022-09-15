package net.kunmc.lab.teamkunpluginmanager.plugin.resolver.interfaces;

import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.QueryContext;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.utils.http.HTTPResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URLをプラグインの直リンクに変換するためのリゾルバのインタフェース
 */
public interface URLResolver extends BaseResolver
{
    /**
     * 使用可能なURLのホスト
     * @return ホスト
     */
    String[] getHosts();

    @Override
    default boolean isValidResolver(QueryContext query)
    {
        try
        {
            URL url = new URL(query.getQuery());

            if (getHosts().length == 0)
                return true;

            for (String host : getHosts())
                if (url.getHost().equalsIgnoreCase(host))
                    return true;
        }
        catch (MalformedURLException e)
        {
            return false;
        }
        return false;
    }

    static String errorCodeWith(String message, int code)
    {
        return message + "(The server responded with " + code + ")。";
    }

    default Matcher urlMatcher(Pattern pattern, String urlString)
    {
        URL url;
        try
        {
            url = new URL(urlString);
        }
        catch (MalformedURLException e)
        {
            return null;
        }

        return pattern.matcher(url.getPath());
    }

    default ErrorResult processErrorResponse(HTTPResponse response, ResolveResult.Source source)
    {
        switch (response.getStatus())
        {
            case URL_MALFORMED:
                return new ErrorResult(this, ErrorResult.ErrorCause.INVALID_QUERY.value("不正なURLが指定されました。"), source);
            case UNABLE_TO_RESOLVE_HOST:
                URL url = null;
                try
                {
                    url = new URL(response.getRequest().getUrl());
                }
                catch (MalformedURLException ignored)
                {
                }

                return new ErrorResult(this,
                        ErrorResult.ErrorCause.INVALID_QUERY.value("ホストが解決できませんでした：" +
                                (url == null ? "Unknown host": url.getHost())), source
                );
            case REDIRECT_LIMIT_EXCEED:
                return new ErrorResult(this,
                        ErrorResult.ErrorCause.SERVER_RESPONSE_MALFORMED.value("リダイレクト回数が上限に達しました。"), source
                );
            case IO_EXCEPTION_OCCURRED:
                return new ErrorResult(this, ErrorResult.ErrorCause.UNKNOWN_ERROR, source);
            case REDIRECT_LOCATION_MALFORMED:
                return new ErrorResult(
                        this,
                        ErrorResult.ErrorCause.SERVER_RESPONSE_MALFORMED.value("リダイレクト先が不正です：" + response.getHeader("Location")),
                        source
                );
        }

        int code = response.getStatusCode();
        switch (code)
        {
            case 200:
                return null;
            case 403:
                return new ErrorResult(this, ErrorResult.ErrorCause.SERVER_RESPONSE_ERROR
                        .value(errorCodeWith("サーバからリソースをダウンロードする権限がありません。", code)), source);
            case 404:
                return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND
                        .value(errorCodeWith("サーバからリソースを見つけることができません。", code)), source);
            case 418:
                return new ErrorResult(this,
                        ErrorResult.ErrorCause.SERVER_RESPONSE_ERROR
                                .value(errorCodeWith("ティーポットでコーヒーを淹れようとしました。", code)), source
                );
            default:
                if (code >= 500 && code < 600)
                    return new ErrorResult(this, ErrorResult.ErrorCause.SERVER_RESPONSE_ERROR
                            .value(errorCodeWith("サーバーがダウンしています。", code)), source);

                return new ErrorResult(this, ErrorResult.ErrorCause.SERVER_RESPONSE_ERROR
                        .value(errorCodeWith("サーバーがエラーレスポンスを返答しました。。", code)),
                        source
                );
        }
    }
}
