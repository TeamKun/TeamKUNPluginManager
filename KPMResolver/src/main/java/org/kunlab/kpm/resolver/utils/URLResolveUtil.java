package org.kunlab.kpm.resolver.utils;

import lombok.experimental.UtilityClass;
import org.kunlab.kpm.http.HTTPResponse;
import org.kunlab.kpm.http.StatusCode;
import org.kunlab.kpm.resolver.ErrorCause;
import org.kunlab.kpm.resolver.interfaces.URLResolver;
import org.kunlab.kpm.resolver.interfaces.result.ErrorResult;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;
import org.kunlab.kpm.resolver.result.ErrorResultImpl;

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

        StatusCode responseCode = response.getStatusCode();
        switch (responseCode)
        {
            case OK:
                return null;
            case UNAUTHORIZED:
                return new ErrorResultImpl(resolver, ErrorCause.INVALID_CREDENTIAL,
                        source,
                        errorCodeWith("Invalid credential", responseCode)
                );
            case FORBIDDEN:
                return new ErrorResultImpl(resolver, ErrorCause.SERVER_RESPONSE_ERROR, source,
                        errorCodeWith("Forbidden", responseCode)
                );
            case NOT_FOUND:
                return new ErrorResultImpl(resolver, ErrorCause.PLUGIN_NOT_FOUND
                        , source, errorCodeWith("Not Found", responseCode)
                );
            case I_AM_A_TEAPOT:
                return new ErrorResultImpl(
                        resolver,
                        ErrorCause.SERVER_RESPONSE_ERROR
                        , source, errorCodeWith("I'm a teapot", responseCode)
                );
            default:
                if (response.isServerError())
                    return new ErrorResultImpl(resolver, ErrorCause.SERVER_RESPONSE_ERROR
                            , source, errorCodeWith("Server Error", responseCode)
                    );
                else
                    return new ErrorResultImpl(resolver, ErrorCause.UNKNOWN_ERROR
                            , source, errorCodeWith("Unknown Error", responseCode)
                    );
        }
    }

    private static String errorCodeWith(String message, StatusCode code)
    {
        return message + "(The server responded with " + code + ")";
    }
}
