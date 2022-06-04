package net.kunmc.lab.teamkunpluginmanager.utils.http;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class RequestContext
{
    String url;

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
    byte[] body = null;

}
