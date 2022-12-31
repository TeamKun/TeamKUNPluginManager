package net.kunmc.lab.kpm.installer.task.tasks.alias.source.download;

import net.kunmc.lab.kpm.installer.task.InstallTask;
import net.kunmc.lab.kpm.installer.task.tasks.alias.source.download.signals.MalformedURLSignal;
import net.kunmc.lab.kpm.installer.task.tasks.alias.source.download.signals.SourceDownloadFailedSignal;
import net.kunmc.lab.kpm.installer.task.tasks.alias.source.download.signals.UnsupportedProtocolSignal;
import net.kunmc.lab.kpm.installer.task.tasks.download.DownloadArgument;
import net.kunmc.lab.kpm.installer.task.tasks.download.DownloadResult;
import net.kunmc.lab.kpm.installer.task.tasks.download.DownloadTask;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * ソースファイルのダウンロードを行うタスクです。
 */
public class SourceDownloadTask extends InstallTask<SourceDownloadArgument, SourceDownloadResult>
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
        HashMap<String, URI> remotes = this.buildURLs(arguments.getRemotes());

        this.status = SourceDownloadState.DOWNLOADING_SOURCES;
        HashMap<String, Pair<URI, Path>> downloadSources = this.downloadSources(remotes);

        if (downloadSources.isEmpty())
            return new SourceDownloadResult(false, this.status, SourceDownloadErrorCause.ALL_DOWNLOAD_FAILED);

        return new SourceDownloadResult(true, this.status, downloadSources);
    }

    private HashMap<String, URI> buildURLs(Map<String, String> sources)
    {
        HashMap<String, URI> result = new HashMap<>();

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

    private HashMap<String, Pair<URI, Path>> downloadSources(HashMap<String, URI> remotes)
    {
        HashMap<String, Pair<URI, Path>> result = new HashMap<>();

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
