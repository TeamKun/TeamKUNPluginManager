package org.kunlab.kpm.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kunlab.kpm.ExceptionHandler;
import org.kunlab.kpm.TokenStore;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@WireMockTest
public class RequestsTest
{
    static void assertSameJsonElement(JsonElement basis, JsonElement test)
    {
        if (basis.isJsonArray())
        {
            assertEquals(basis.getAsJsonArray().size(), test.getAsJsonArray().size());

            Iterator<JsonElement> it = basis.getAsJsonArray().iterator();
            Iterator<JsonElement> it2 = test.getAsJsonArray().iterator();
            while (it.hasNext() && it2.hasNext())
                assertSameJson(it.next().toString(), it2.next());
        }
        else if (basis.isJsonObject())
        {
            Iterator<Map.Entry<String, JsonElement>> it = basis.getAsJsonObject().entrySet().iterator();
            Iterator<Map.Entry<String, JsonElement>> it2 = test.getAsJsonObject().entrySet().iterator();
            while (it.hasNext() && it2.hasNext())
            {
                Map.Entry<String, JsonElement> entry = it.next();
                Map.Entry<String, JsonElement> entry2 = it2.next();
                assertEquals(entry.getKey(), entry2.getKey());
                assertSameJsonElement(entry.getValue(), entry2.getValue());
            }
        }
        else
            assertEquals(basis, test);
    }

    static void assertSameJson(String jsonString, JsonElement gsonElement)
    {
        Gson gson = new Gson();
        JsonElement node = gson.fromJson(jsonString, JsonElement.class);

        assertSameJsonElement(node, gsonElement);
    }

    @BeforeAll
    @SneakyThrows(IOException.class)
    static void setup()
    {
        Path tokenPath = File.createTempFile("test_token", ".dat").toPath();
        Path keyPath = File.createTempFile("test_key", ".dat").toPath();

        TokenStore store = new TokenStore(tokenPath, keyPath, Mockito.mock(ExceptionHandler.class));

        Requests.setTokenStore(store);
        Requests.setRedirectLimit(15);
        Requests.setConnectTimeout(0);
    }

    static void testAnyMethod(WireMockRuntimeInfo mockInfo, RequestMethod method) throws IOException
    {
        try (HTTPResponse response = Requests.request(RequestContext.builder()
                .url(mockInfo.getHttpBaseUrl() + "/test")
                .method(method)
                .build()))
        {
            assertEquals(StatusCode.OK, response.getStatusCode());
            assertSameJson("{\"test\": \"test\"}", response.getAsJson());
        }
    }

    static void createStub(WireMockRuntimeInfo mockInfo)
    {
        mockInfo.getWireMock().register(any(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"test\": \"test\"}")));
    }

    @Test
    @SneakyThrows(IOException.class)
    void 標準的なリクエストのテスト(WireMockRuntimeInfo mockInfo)
    {
        createStub(mockInfo);

        testAnyMethod(mockInfo, RequestMethod.GET);
        testAnyMethod(mockInfo, RequestMethod.POST);
        testAnyMethod(mockInfo, RequestMethod.PUT);
        testAnyMethod(mockInfo, RequestMethod.DELETE);
        testAnyMethod(mockInfo, RequestMethod.OPTIONS);
        // HEADはボディを返さないので別途テスト
        testAnyMethod(mockInfo, RequestMethod.TRACE);
    }

    @Test
    @SneakyThrows(IOException.class)
    void HEADリクエストのボディが空になるか(WireMockRuntimeInfo mockInfo)
    {
        createStub(mockInfo);

        try (HTTPResponse response = Requests.request(RequestContext.builder()
                .url(mockInfo.getHttpBaseUrl() + "/test")
                .method(RequestMethod.HEAD)
                .build()))
        {
            assertEquals(StatusCode.OK, response.getStatusCode());
            assertNull(response.getAsJson());
        }
    }

}
