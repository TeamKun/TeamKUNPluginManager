package net.kunmc.lab.kpm.resolver.utils;

import lombok.experimental.UtilityClass;
import net.kunmc.lab.kpm.enums.resolver.ErrorCause;
import net.kunmc.lab.kpm.http.HTTPResponse;
import net.kunmc.lab.kpm.interfaces.resolver.URLResolver;
import net.kunmc.lab.kpm.interfaces.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.result.ErrorResultImpl;

import java.net.MalformedURLException;
import java.net.URL;

@UtilityClass
public class URLResolveUtil
{
    public static ErrorResult processErrorResponse(URLResolver resolver, HTTPResponse response, ResolveResult.Source source)
    {
        switch (response.getStatus())
        {
            case URL_MALFORMED:
                return new ErrorResultImpl(resolver, ErrorCause.INVALID_QUERY, source, "URL Malformed.");
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

                return new ErrorResultImpl(resolver,
                        ErrorCause.HOST_RESOLVE_FAILED, source,
                        "Unable to resolve host " + url.getHost() + "."
                );
            case REDIRECT_LIMIT_EXCEED:
                return new ErrorResultImpl(resolver,
                        ErrorCause.SERVER_RESPONSE_MALFORMED, source, "Redirect limit exceed."
                );
            case IO_EXCEPTION_OCCURRED:
                return new ErrorResultImpl(resolver, ErrorCause.UNKNOWN_ERROR, source);
            case REDIRECT_LOCATION_MALFORMED:
                return new ErrorResultImpl(
                        resolver,
                        ErrorCause.SERVER_RESPONSE_MALFORMED,
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
                return new ErrorResultImpl(resolver, ErrorCause.INVALID_CREDENTIAL,
                        source,
                        errorCodeWith("Invalid credential", code)
                );
            case 403:
                return new ErrorResultImpl(resolver, ErrorCause.SERVER_RESPONSE_ERROR, source,
                        errorCodeWith("Forbidden", code)
                );
            case 404:
                return new ErrorResultImpl(resolver, ErrorCause.PLUGIN_NOT_FOUND
                        , source, errorCodeWith("Not Found", code)
                );
            case 418:
                return new ErrorResultImpl(
                        resolver,
                        ErrorCause.SERVER_RESPONSE_ERROR
                        , source, errorCodeWith("I'm a teapot", code)
                );
            default:
                if (code >= 500 && code < 600)
                    return new ErrorResultImpl(resolver, ErrorCause.SERVER_RESPONSE_ERROR
                            , source, errorCodeWith("Server Error", code)
                    );
                else
                    return new ErrorResultImpl(resolver, ErrorCause.UNKNOWN_ERROR
                            , source, errorCodeWith("Unknown Error", code)
                    );
        }
    }

    private static String errorCodeWith(String message, int code)
    {
        return message + "(The server responded with " + code + ")";
    }
}
