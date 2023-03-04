package org.kunlab.kpm.task.tasks.alias.source.download;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.interfaces.InstallerArgument;
import org.kunlab.kpm.installer.interfaces.PluginInstaller;
import org.kunlab.kpm.task.AbstractInstallTask;
import org.kunlab.kpm.task.tasks.alias.source.download.signals.MalformedURLSignal;
import org.kunlab.kpm.task.tasks.alias.source.download.signals.SourceDownloadFailedSignal;
import org.kunlab.kpm.task.tasks.alias.source.download.signals.UnsupportedProtocolSignal;
import org.kunlab.kpm.task.tasks.download.DownloadArgument;
import org.kunlab.kpm.task.tasks.download.DownloadResult;
import org.kunlab.kpm.task.tasks.download.DownloadTask;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * ソースファイルのダウンロードを行うタスクです。
 */
public class SourceDownloadTask extends AbstractInstallTask<SourceDownloadArgument, SourceDownloadResult>
{
    private SourceDownloadState status;

    public SourceDownloadTask(@NotNull PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>> installer)
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
        Map<String, URI> remotes = this.buildURLs(arguments.getRemotes());

        this.status = SourceDownloadState.DOWNLOADING_SOURCES;
        Map<String, Pair<URI, Path>> downloadSources = this.downloadSources(remotes);

        if (downloadSources.isEmpty())
            return new SourceDownloadResult(false, this.status, SourceDownloadErrorCause.ALL_DOWNLOAD_FAILED);

        return new SourceDownloadResult(true, this.status, downloadSources);
    }

    private Map<String, URI> buildURLs(Map<String, String> sources)
    {
        Map<String, URI> result = new HashMap<>();

        for (String remoteName : sources.keySet())
        {
            String remoteURL = sources.get(remoteName);

            URI url;
            if ((url = this.buildURI(remoteName, remoteURL)) != null)
                result.put(remoteName, url);
        }

        return result;
    }

    private URI buildURI(String fallbackName, String urlString)
    {
        URI url;
        try
        {
            url = new URI(urlString);
        }
        catch (URISyntaxException e)
        {
            this.postSignal(new MalformedURLSignal(fallbackName, urlString));
            return null;
        }

        if (!isProtocolSupported(url.getScheme()))
        {
            try
            {
                this.postSignal(new UnsupportedProtocolSignal(fallbackName, url.toURL()));
            }
            catch (MalformedURLException ignored)
            {
            }
            return null;
        }

        return url;
    }

    private Map<String, Pair<URI, Path>> downloadSources(Map<String, URI> remotes)
    {
        Map<String, Pair<URI, Path>> result = new HashMap<>();

        for (String remoteName : remotes.keySet())
        {
            URI remoteURL = remotes.get(remoteName);

            Path path;
            if ((path = this.downloadSource(remoteName, remoteURL)) != null)
                result.put(remoteName, new Pair<>(remoteURL, path));
        }

        return result;
    }

    private Path downloadSource(String remoteName, URI remoteURL)
    {
        DownloadResult result = new DownloadTask(this.progress.getInstaller())
                .runTask(new DownloadArgument(remoteURL.toString()));
        boolean success = result.isSuccess();

        if (!success)
            this.postSignal(new SourceDownloadFailedSignal(remoteName, result));

        return success ? result.getPath(): null;
    }
}
