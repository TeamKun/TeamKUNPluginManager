package net.kunmc.lab.teamkunpluginmanager.result.http;

import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.utils.http.HTTPResponse;
import net.kunmc.lab.teamkunpluginmanager.utils.http.RequestContext;
import net.kunmc.lab.teamkunpluginmanager.utils.http.Requests;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class RequestsTest
{
    private static final String mockUrl = "https://run.mocky.io/v3/63f730d5-cb39-413e-a56f-37450aea4df0";
    private static final String redirMockURl = "https://run.mocky.io/v3/3df2a3b9-c9fd-4b44-a69c-2c408d9f93fa";

    @Test
    public void testGetNoRedir()
    {
        HTTPResponse response = Requests.request(RequestContext.builder()
                .url(mockUrl)
                .build());

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(response.getHeader("Content-Type"), "application/json; charset=UTF-8");

        JsonObject json = response.getAsJson().getAsJsonObject();

        Assert.assertEquals(json.get("hello").getAsString(), "world");
    }

    @Test
    public void testGetRedir()
    {
        HTTPResponse response = Requests.request(RequestContext.builder()
                .url(redirMockURl)
                .followRedirects(true)
                .build());

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(response.getHeader("Content-Type"), "application/json; charset=UTF-8");

        JsonObject json = response.getAsJson().getAsJsonObject();

        Assert.assertEquals(json.get("hello").getAsString(), "world");
    }

    @Test
    public void testGetRedirButNoRedir()
    {
        HTTPResponse response = Requests.request(RequestContext.builder()
                .url(redirMockURl)
                .followRedirects(false)
                .build());

        Assert.assertEquals(301, response.getStatusCode());
        Assert.assertEquals(response.getHeader("Location"), mockUrl);
    }

    @Test
    public void testGetCache()
    {
        HTTPResponse response = Requests.request(RequestContext.builder()
                .url(mockUrl)
                .build());

        String first = response.getAsString();
        String second = response.getAsString();

        Assert.assertEquals(first, second);
    }
}
