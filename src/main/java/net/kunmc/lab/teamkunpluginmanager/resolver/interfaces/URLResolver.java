package net.kunmc.lab.teamkunpluginmanager.resolver.interfaces;

import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * URLをプラグインの直リンクに変換するためのリゾルバのインタフェース
 */
public interface URLResolver extends BaseResolver
{
    /**
     * 使用可能なURLのパターン
     * @return パターン
     */
    Pattern[] getURLPatterns();

    @Override
    default boolean isValidResolver(String query)
    {
        try
        {
            new URL(query);
        }
        catch (MalformedURLException e)
        {
            return false;
        }

        Pattern[] patterns = getURLPatterns();
        if (patterns == null || patterns.length == 0)
            return true;

        for (Pattern pattern : patterns)
            if (pattern.matcher(query).matches())
                return true;

        return false;
    }

    static String errorCodeWith(String message, int code)
    {
        return message + "(The server responded with " + code + ")。";
    }

    default ErrorResult processErrorResponse(int code, ResolveResult.Source source)
    {
        ErrorResult.ErrorCause cause = ErrorResult.ErrorCause.SERVER_RESPONSE_ERROR;

        switch (code)
        {
            case 200:
                return null;
            case 403:
                return new ErrorResult(cause
                        .value(errorCodeWith("サーバからリソースをダウンロードする権限がありません。", code)), source);
            case 404:
                return new ErrorResult(cause
                        .value(errorCodeWith("サーバからリソースを見つけることができません。", code)), source);
            case 418:
                return new ErrorResult(cause.value(errorCodeWith("ティーポットでコーヒーを淹れようとしました。", code)), source);
            default:
                if (code >= 500 && code < 600)
                    return new ErrorResult(cause
                            .value(errorCodeWith("サーバーがダウンしています。", code)), source);

                return new ErrorResult(cause
                        .value(errorCodeWith("サーバーがエラーレスポンスを返答しました。。", code)),
                        source);
        }
    }
}
