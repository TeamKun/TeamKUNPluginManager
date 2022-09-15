package net.kunmc.lab.teamkunpluginmanager.utils.http;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Requests
{
    @Getter
    @Setter
    private static int REDIRECT_LIMIT = 15;

    private static HTTPResponse doRedirect(HTTPResponse response, int redirectCount)
    {
        if (!response.isRedirect())
            return response;

        RequestContext request = response.getRequest();

        String location = response.getHeader("Location");
        if (location == null)
            return HTTPResponse.error(request, HTTPResponse.RequestStatus.REDIRECT_LOCATION_MALFORMED);

        if (redirectCount > REDIRECT_LIMIT)
            return HTTPResponse.error(request, HTTPResponse.RequestStatus.REDIRECT_LIMIT_EXCEED);

        HTTPResponse newResponse = request(RequestContext.builder()
                .cacheable(request.isCacheable())
                .method(request.getMethod())
                .extraHeaders(request.getExtraHeaders())
                .followRedirects(false)
                .timeout(request.getTimeout())
                .body(request.getBody())
                .url(location)
                .build());

        return doRedirect(newResponse, redirectCount + 1);
    }

    private static Map<String, String> setupDefaultHeaders(@NotNull String host)
    {
        HashMap<String, String> headers = new HashMap<>();

        headers.put("User-Agent", "Mozilla/8.10 (X931; Peyantu; Linux x86_64) PeyangWebKit/114.514(KUN, like Gacho) TeamKunPluginManager/" +
                TeamKunPluginManager.getPlugin().getDescription().getVersion());

        if (host.equalsIgnoreCase("github.com") ||
                StringUtils.endsWithIgnoreCase(host, ".github.com") ||
                StringUtils.endsWithIgnoreCase(host, ".githubusercontent.com"))
        {
            headers.put("Accept", "application/vnd.github.v3+json");

            if (TeamKunPluginManager.getPlugin().isTokenAvailable())
                headers.put("Authorization", "Token " + TeamKunPluginManager.getPlugin().getTokenStore().getToken());

        }
        else if (host.equalsIgnoreCase("file.io"))
            headers.put("Referer", "https://www.file.io/");


        return headers;
    }

    @NotNull
    public static HTTPResponse request(@NotNull RequestContext context)
    {
        if (context.getUrl() == null)
            throw new IllegalArgumentException("URL is null");

        try
        {
            URL url = new URL(context.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod(context.getMethod().name());
            connection.setUseCaches(false);
            connection.setConnectTimeout(10000);

            if (context.getTimeout() > 0)
                connection.setReadTimeout(context.getTimeout());

            for (Map.Entry<String, String> entry : context.getExtraHeaders().entrySet())
                connection.setRequestProperty(entry.getKey(), entry.getValue());

            setupDefaultHeaders(url.getHost())
                    .forEach(connection::setRequestProperty);

            connection.connect();

            if (context.getBody() != null)
                try (OutputStream outputStream = connection.getOutputStream())
                {
                    outputStream.write(context.getBody());
                }

            int responseCode = connection.getResponseCode();

            HashMap<String, String> serverHeaders = connection.getHeaderFields().entrySet().stream().parallel()
                    .map(stringListEntry -> new AbstractMap.SimpleEntry<>(
                            stringListEntry.getKey() == null ? null: stringListEntry.getKey().toLowerCase(),
                            String.join(" ", stringListEntry.getValue())
                    ))
                    .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);

            String protocol = "HTTP";
            String protocolVersion = "1.1";

            if (serverHeaders.containsKey(null))
            {
                String[] protocolAndStatus = StringUtils.split(serverHeaders.get(null), " ");
                if (protocolAndStatus.length > 1)
                {
                    String[] protocolAndVersion = StringUtils.split(protocolAndStatus[0], "/");
                    if (protocolAndVersion.length > 0)
                        protocol = protocolAndVersion[0];
                    if (protocolAndVersion.length > 1)
                        protocolVersion = protocolAndVersion[1];
                }

                serverHeaders.remove(null);
            }

            HTTPResponse.RequestStatus status = HTTPResponse.RequestStatus.OK;
            if (responseCode >= 500)
                status = HTTPResponse.RequestStatus.SERVER_ERROR;
            else if (responseCode >= 400)
                status = HTTPResponse.RequestStatus.CLIENT_ERROR;

            HTTPResponse response = new HTTPResponse(status,
                    context, protocol, protocolVersion, responseCode, serverHeaders,
                    responseCode >= 400 ? connection.getErrorStream(): connection.getInputStream()
            );

            if (context.isFollowRedirects())
                return doRedirect(response, 0);

            return response;
        }
        catch (MalformedURLException ex)
        {
            return HTTPResponse.error(context, HTTPResponse.RequestStatus.URL_MALFORMED);
        }
        catch (UnknownHostException ex)
        {
            return HTTPResponse.error(context, HTTPResponse.RequestStatus.UNABLE_TO_RESOLVE_HOST);
        }
        catch (IOException ex)
        {
            return HTTPResponse.error(context, HTTPResponse.RequestStatus.IO_EXCEPTION_OCCURRED);
        }
    }

    /**
     * Download a file from a URL
     *
     * @param method     The HTTP method to use
     * @param url        The URL to download from
     * @param path       The path to save the file to
     * @param onProgress A callback to be called when the download progresses.
     * @return The downloaded file size or -1 if the download failed.
     * @throws IOException If the download failed
     */
    public static long downloadFile(@NotNull RequestMethod method, @NotNull String url,
                                    @NotNull Path path, @Nullable Consumer<DownloadProgress> onProgress) throws IOException
    {
        try (HTTPResponse response = request(RequestContext.builder()
                .url(url)
                .method(method)
                .followRedirects(true)
                .build());
             OutputStream output = Files.newOutputStream(path))
        {
            if (response.getStatusCode() < 200 || response.getStatusCode() >= 300)
                throw new IOException("HTTP error " + response.getStatusCode());
            else if (response.getInputStream() == null)
                throw new IOException("No response body was returned");

            String contentLength = response.getHeader("Content-Length");

            long size = contentLength != null ? Long.parseLong(contentLength): -1;


            byte[] buffer = new byte[1024];

            int read;
            long downloaded = 0;
            while ((read = response.getInputStream().read(buffer)) != -1)
            {
                output.write(buffer, 0, read);
                downloaded += read;

                if (onProgress == null)
                    continue;

                int progress;
                if (downloaded != 0 && size != 0)
                    progress = (int) (downloaded * 100 / size);
                else
                    progress = 0;

                onProgress.accept(new DownloadProgress(size, downloaded, progress));
            }

            return size;
        }
        catch (IOException e)
        {
            if (Files.exists(path))
                Files.delete(path);

            throw e;
        }
    }

    public static long downloadFile(@NotNull RequestMethod method, @NotNull String url,
                                    @NotNull Path path) throws IOException
    {
        return downloadFile(method, url, path, null);
    }

}
