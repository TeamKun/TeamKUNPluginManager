package net.kunmc.lab.kpm.installer.task.tasks.alias.source.download;

import net.kunmc.lab.kpm.installer.AbstractInstaller;
import net.kunmc.lab.kpm.installer.task.InstallTask;
import net.kunmc.lab.kpm.installer.task.tasks.alias.source.download.signals.MalformedURLSignal;
import net.kunmc.lab.kpm.installer.task.tasks.alias.source.download.signals.SourceDownloadFailedSignal;
import net.kunmc.lab.kpm.installer.task.tasks.alias.source.download.signals.UnsupportedProtocolSignal;
import net.kunmc.lab.kpm.installer.task.tasks.download.DownloadArgument;
import net.kunmc.lab.kpm.installer.task.tasks.download.DownloadResult;
import net.kunmc.lab.kpm.installer.task.tasks.download.DownloadTask;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * ソースファイルのダウンロードを行うタスクです。
 */
public class SourceDownloadTask extends InstallTask<SourceDownloadArgument, SourceDownloadResult>
{
    private SourceDownloadState status;

    public SourceDownloadTask(@NotNull AbstractInstaller<?, ?, ?> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());

        this.status = SourceDownloadState.INITIALIZED;
    }

    private static boolean isProtocolSupported(String protocolName)
    {
        return protocolName.equalsIgnoreCase("http") || protocolName.equalsIgnoreCase("https");
    }

    @Override
    public @NotNull SourceDownloadResult runTask(@NotNull SourceDownloadArgument arguments)
    {
        this.status = SourceDownloadState.ANALYZING_URLS;
        HashMap<String, URL> remotes = this.buildURLs(arguments.getRemotes());

        this.status = SourceDownloadState.DOWNLOADING_SOURCES;
        HashMap<String, Pair<URL, Path>> downloadSources = this.downloadSources(remotes);

        if (downloadSources.isEmpty())
            return new SourceDownloadResult(false, this.status, SourceDownloadErrorCause.ALL_DOWNLOAD_FAILED);

        return new SourceDownloadResult(true, this.status, downloadSources);
    }

    private HashMap<String, URL> buildURLs(Map<String, String> sources)
    {
        HashMap<String, URL> result = new HashMap<>();  // TODO: URL to URI

        for (String remoteName : sources.keySet())
        {
            String remoteURL = sources.get(remoteName);

            URL url;
            if ((url = this.buildURL(remoteName, remoteURL)) != null)
                result.put(remoteName, url);
        }

        return result;
    }

    private URL buildURL(String fallbackName, String urlString)
    {
        URL url;
        try
        {
            url = new URL(urlString);
        }
        catch (MalformedURLException e)
        {
            this.postSignal(new MalformedURLSignal(fallbackName, urlString));
            return null;
        }

        if (!isProtocolSupported(url.getProtocol()))
        {
            this.postSignal(new UnsupportedProtocolSignal(fallbackName, url));
            return null;
        }

        return url;
    }

    private HashMap<String, Pair<URL, Path>> downloadSources(HashMap<String, URL> remotes)
    {
        HashMap<String, Pair<URL, Path>> result = new HashMap<>();

        for (String remoteName : remotes.keySet())
        {
            URL remoteURL = remotes.get(remoteName);

            Path path;
            if ((path = this.downloadSource(remoteName, remoteURL)) != null)
                result.put(remoteName, new Pair<>(remoteURL, path));
        }

        return result;
    }

    private Path downloadSource(String remoteName, URL remoteURL)
    {
        DownloadResult result = new DownloadTask(this.progress.getInstaller())
                .runTask(new DownloadArgument(remoteURL.toString()));
        boolean success = result.isSuccess();

        if (!success)
            this.postSignal(new SourceDownloadFailedSignal(remoteName, result));

        return success ? result.getPath(): null;
    }
}
