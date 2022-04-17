package net.kunmc.lab.teamkunpluginmanager.utils.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@Value
public class HTTPResponse implements AutoCloseable
{
    RequestStatus status;
    RequestContext request;

    String serverProtocol;
    String protocolVersion;

    int statusCode;

    HashMap<String, String> headers;

    @Nullable
    InputStream inputStream;

    public String getAsString()
    {
        if (inputStream == null)
            return null;
        StringBuilder sb = new StringBuilder();

        byte[] buffer = new byte[1024];
        int len;
        try
        {
            while ((len = inputStream.read(buffer)) != -1)
                sb.append(new String(buffer, 0, len));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public JsonElement getAsJson()
    {
        if (inputStream == null)
            return null;

        String json = getAsString();
        return new Gson().fromJson(json, JsonElement.class);
    }

    @Override
    public void close() throws IOException
    {
        if (inputStream != null)
            this.inputStream.close();
    }

    @Nullable
    public String getHeader(@NotNull String header)
    {
        return headers.getOrDefault(header.toLowerCase(), headers.get(header));
    }

    public boolean isSuccessful()
    {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isRedirect()
    {
        return statusCode >= 300 && statusCode < 400;
    }

    public boolean isClientError()
    {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError()
    {
        return statusCode >= 500 && statusCode < 600;
    }

    public boolean isError()
    {
        return isClientError() || isServerError();
    }

    public boolean isOK()
    {
        return statusCode == 200;
    }

    public static HTTPResponse error(@NotNull RequestContext request, @NotNull RequestStatus status)
    {
        return new HTTPResponse(status, request, null, null, -1, null, null);
    }

    public enum RequestStatus
    {
        OK,
        REDIRECT_LOCATION_MALFORMED,
        REDIRECT_LIMIT_EXCEED,
        UNABLE_TO_RESOLVE_HOST,
        IO_EXCEPTION_OCCURRED,
        URL_MALFORMED
    }
}
