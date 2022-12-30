package net.kunmc.lab.kpm.resolver.interfaces;

import net.kunmc.lab.kpm.http.HTTPResponse;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL を解決するリゾルバのインターフェースです。
 */
public interface URLResolver extends BaseResolver
{
    static String errorCodeWith(String message, int code)
    {
        return message + "(The server responded with " + code + ")";
    }

    /**
     * このリゾルバが対応してるURLのホスト名を返します。
     *
     * @return このリゾルバが対応してるURLのホスト名
     */
    String[] getHosts();

    /**
     * このリゾルバがクエリに対応しているかを返します。
     *
     * @param query クエリ (URL)
     * @return このリゾルバがクエリに対応しているか
     */
    @Override
    default boolean isValidResolver(QueryContext query)
    {
        try
        {
            URL url = new URL(query.getQuery());

            if (this.getHosts().length == 0)
                return true;

            for (String host : this.getHosts())
                if (url.getHost().equalsIgnoreCase(host))
                    return true;
        }
        catch (MalformedURLException e)
        {
            return false;
        }
        return false;
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
                return new ErrorResult(this, ErrorResult.ErrorCause.INVALID_QUERY, source, "URL Malformed.");
            case UNABLE_TO_RESOLVE_HOST:
                URL url;
                try
                {
                    url = new URL(response.getRequest().getUrl());
                }
                catch (MalformedURLException e)
                {
                    throw new IllegalStateException("Illegal URL: " + response.getRequest().getUrl());
                }

                return new ErrorResult(this,
                        ErrorResult.ErrorCause.HOST_RESOLVE_FAILED, source,
                        "Unable to resolve host " + url.getHost() + "."
                );
            case REDIRECT_LIMIT_EXCEED:
                return new ErrorResult(this,
                        ErrorResult.ErrorCause.SERVER_RESPONSE_MALFORMED, source, "Redirect limit exceed."
                );
            case IO_EXCEPTION_OCCURRED:
                return new ErrorResult(this, ErrorResult.ErrorCause.UNKNOWN_ERROR, source);
            case REDIRECT_LOCATION_MALFORMED:
                return new ErrorResult(
                        this,
                        ErrorResult.ErrorCause.SERVER_RESPONSE_MALFORMED,
                        source,
                        "Redirect location malformed: " + response.getHeader("Location")
                );
        }

        int code = response.getStatusCode();
        switch (code)
        {
            case 200:
                return null;
            case 401:
                return new ErrorResult(this, ErrorResult.ErrorCause.INVALID_CREDENTIAL,
                        source,
                        errorCodeWith("Invalid credential", code)
                );
            case 403:
                return new ErrorResult(this, ErrorResult.ErrorCause.SERVER_RESPONSE_ERROR, source,
                        errorCodeWith("Forbidden", code)
                );
            case 404:
                return new ErrorResult(this, ErrorResult.ErrorCause.PLUGIN_NOT_FOUND
                        , source, errorCodeWith("Not Found", code)
                );
            case 418:
                return new ErrorResult(
                        this,
                        ErrorResult.ErrorCause.SERVER_RESPONSE_ERROR
                        , source, errorCodeWith("I'm a teapot", code)
                );
            default:
                if (code >= 500 && code < 600)
                    return new ErrorResult(this, ErrorResult.ErrorCause.SERVER_RESPONSE_ERROR
                            , source, errorCodeWith("Server Error", code)
                    );
                else
                    return new ErrorResult(this, ErrorResult.ErrorCause.UNKNOWN_ERROR
                            , source, errorCodeWith("Unknown Error", code)
                    );
        }
    }
}
