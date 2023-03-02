package org.kunlab.kpm.http;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.DebugConstants;
import org.kunlab.kpm.TokenStore;
import org.kunlab.kpm.utils.KPMCollectors;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * HTTP リクエストを補助するクラスです。
 */
@UtilityClass
public class Requests
{
    @Setter
    private static TokenStore tokenStore;
    @Getter
    @NotNull
    private static Map<String, String> extraHeaders;

    @Getter
    @Setter
    private static int redirectLimit = 15;
    @Getter
    @Setter
    private static int connectTimeout = 10000;

    static
    {
        extraHeaders = new HashMap<>();
    }

    private static HTTPResponse doRedirect(HTTPResponse response, int redirectCount)
    {
        if (!response.isRedirect())
            return response;

        RequestContext request = response.getRequest();

        String location = response.getHeader("Location");
        if (location == null)
            return HTTPResponse.error(request, HTTPResponse.RequestStatus.REDIRECT_LOCATION_MALFORMED);

        if (redirectCount > redirectLimit)
            return HTTPResponse.error(request, HTTPResponse.RequestStatus.REDIRECT_LIMIT_EXCEED);

        if (DebugConstants.HTTP_REDIRECT_TRACE)
            System.out.println("Redirecting from " + request.getUrl() + " to " + location + " (count: " + redirectCount + ")");

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

    @NotNull
    private static Map<String, String> setupDefaultHeaders(@NotNull String host, @NotNull Map<String, String> currentHeaders)
    {
        HashMap<String, String> headers = new HashMap<>();

        if (host.equalsIgnoreCase("github.com") ||
                StringUtils.endsWithIgnoreCase(host, ".github.com") ||
                StringUtils.endsWithIgnoreCase(host, ".githubusercontent.com"))
        {
            headers.put("Accept", "application/vnd.github.v3+json");

            if (tokenStore.isTokenAvailable())
                headers.put(
                        "Authorization",
                        "Token " + tokenStore.getToken()
                );

        }
        else if (host.equalsIgnoreCase("file.io"))
            headers.put("Referer", "https://www.file.io/");

        headers.putAll(extraHeaders);
        headers.putAll(currentHeaders);


        return headers;
    }

    /**
     * リクエストを送信し、レスポンスを受け取ります。
     *
     * @param context リクエストのコンテキスト
     * @return レスポンス
     */
    @NotNull
    public static HTTPResponse request(@NotNull RequestContext context)
    {
        if (context.getUrl() == null)
            throw new IllegalArgumentException("URL is null");

        if (DebugConstants.HTTP_REQUEST_TRACE)
        {
            System.out.println("Requesting " + context.getMethod() + " " + context.getUrl());
            System.out.println("Headers:" + context.getExtraHeaders().entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", ")));
        }

        try
        {
            HttpURLConnection connection = createConnection(context);
            connection.connect();

            if (context.getBody() != null)
                try (OutputStream outputStream = connection.getOutputStream())
                {
                    outputStream.write(context.getBody());
                }

            int responseCode = connection.getResponseCode();

            HashMap<String, String> serverHeaders = buildHeaders(connection.getHeaderFields());
            Pair<String, String> serverProtocol = retrieveProtocol(serverHeaders);  // serverHeaders will be modified in this method

            HTTPResponse.RequestStatus status = HTTPResponse.RequestStatus.OK;
            if (responseCode >= 500)
                status = HTTPResponse.RequestStatus.SERVER_ERROR;
            else if (responseCode >= 400)
                status = HTTPResponse.RequestStatus.CLIENT_ERROR;

            HTTPResponse response = new HTTPResponse(status,
                    context, serverProtocol.getLeft(), serverProtocol.getRight(), responseCode, serverHeaders,
                    responseCode >= 400 ? connection.getErrorStream(): connection.getInputStream()
            );

            if (DebugConstants.HTTP_REQUEST_TRACE)
            {
                System.out.println("Response from " + context.getUrl() + ": " + responseCode + " " + response.getStatus());
                System.out.println("Headers:" + response.getHeaders().entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining(", ")));

            }

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

    private static HttpURLConnection createConnection(RequestContext context) throws IOException
    {
        URL url = new URL(context.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod(context.getMethod().name());
        connection.setUseCaches(false);
        connection.setConnectTimeout(10000);

        if (context.getTimeout() > 0)
            connection.setReadTimeout(context.getTimeout());
        if (context.getMethod() == RequestMethod.POST)
            connection.setDoOutput(true);

        for (Map.Entry<String, String> entry : context.getExtraHeaders().entrySet())
            connection.setRequestProperty(entry.getKey(), entry.getValue());

        setupDefaultHeaders(url.getHost(), context.getExtraHeaders())
                .forEach(connection::setRequestProperty);

        return connection;
    }

    private static HashMap<String, String> buildHeaders(Map<String, List<String>> originalHeaders)
    {
        return originalHeaders.entrySet().stream().parallel()
                .map(stringListEntry -> Pair.of(
                        stringListEntry.getKey() == null ? null: stringListEntry.getKey().toLowerCase(),
                        String.join(" ", stringListEntry.getValue())
                ))
                .collect(KPMCollectors.toPairHashMap());
    }

    private static Pair<String, String> retrieveProtocol(Map<String, String> serverHeaders)
    {
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

        return new Pair<>(protocol, protocolVersion);
    }

    /**
     * URLを使用してファイルをダウンロードします。
     *
     * @param method     リクエストメソッド
     * @param url        URL
     * @param path       ダウンロード先のパス
     * @param onProgress ダウンロードの進捗を通知するコールバック
     * @return ダウンロードしたファイルのパス
     * @throws IOException ダウンロードに失敗した場合
     */
    public static long downloadFile(@NotNull RequestMethod method, @NotNull String url,
                                    @NotNull Path path, @Nullable Consumer<DownloadProgress> onProgress) throws IOException
    {
        RequestContext.RequestContextBuilder context = RequestContext.builder()
                .url(url)
                .method(method)
                .followRedirects(true);

        if (url.matches("^https?://api\\.github\\.com/.+?/releases/assets/\\d+$"))
            context.header("Accept", "application/octet-stream");

        try (HTTPResponse response = request(context.build());
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

    /**
     * URLを使用してファイルをダウンロードします。
     *
     * @param method リクエストメソッド
     * @param url    URL
     * @param path   ダウンロード先のパス
     * @return ファイルの大きさ
     * @throws IOException ダウンロードに失敗した場合
     */
    public static long downloadFile(@NotNull RequestMethod method, @NotNull String url,
                                    @NotNull Path path) throws IOException
    {
        return downloadFile(method, url, path, null);
    }
}
