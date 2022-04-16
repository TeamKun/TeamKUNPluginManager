package net.kunmc.lab.teamkunpluginmanager.utils.http;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.net.URL;
import java.util.Map;

@Value
@Builder
public class RequestContext
{
    URL url;

    @Builder.Default
    RequestMethod method = RequestMethod.GET;

    @Singular("header")
    Map<String, String> extraHeaders;

    @Builder.Default
    boolean cacheable = false;
    @Builder.Default
    boolean followRedirects = true;
    @Builder.Default
    int timeout = -1;

    @Builder.Default
    byte[] body = {};

    public enum RequestMethod
    {
        GET,
        POST,
        PUT,
        DELETE,
        HEAD,
        OPTIONS,
        TRACE
    }
}
