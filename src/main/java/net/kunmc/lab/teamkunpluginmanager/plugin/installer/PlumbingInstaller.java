package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download.DownloadErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download.DownloadErrorSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download.DownloadProgressSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download.DownloadStartedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.download.DownloadSucceedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.resolve.MultiplePluginResolvedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.resolve.PluginResolveErrorSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signal.signals.resolve.PluginResolvingSignal;
import net.kunmc.lab.teamkunpluginmanager.resolver.PluginResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import net.kunmc.lab.teamkunpluginmanager.utils.http.DownloadProgress;
import net.kunmc.lab.teamkunpluginmanager.utils.http.RequestMethod;
import net.kunmc.lab.teamkunpluginmanager.utils.http.Requests;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class PlumbingInstaller
{
    private static final PluginResolver PLUGIN_RESOLVER;

    static
    {
        PLUGIN_RESOLVER = TeamKunPluginManager.getPlugin().getResolver();
    }

    private final InstallerSignalHandler signalHandler;

    @Getter
    private final InstallProgress progress;

    public static PlumbingInstaller initInstall(InstallerSignalHandler signalHandler)
            throws IOException, SecurityException
    {

        InstallProgress progress = new InstallProgress(true);

        PlumbingInstaller instance = new PlumbingInstaller(signalHandler, progress);

        progress.setPhase(InstallPhase.INITIALIZING);
        return instance;
    }

    public @NotNull ResolveResult resolvePlugin(@NotNull String query)
    {
        this.signalHandler.handleSignal(new PluginResolvingSignal(query, PLUGIN_RESOLVER));

        return PLUGIN_RESOLVER.resolve(query);
    }

    public @Nullable ResolveResult resolveMultipleResults(@NotNull String query, @NotNull MultiResult results)
    {
        if (results.getResults().length < 1)
            throw new IllegalStateException("MultiResult with no results.");

        MultiplePluginResolvedSignal signal = new MultiplePluginResolvedSignal(query, results);
        this.signalHandler.handleSignal(signal);

        if (signal.getSpecifiedResult() != null)
            return signal.getSpecifiedResult(); // Plugin actually resolved by SignalHandler.

        ResolveResult result = results.getResults()[0];

        if (result instanceof MultiResult)
        {
            MultiResult multiResult = (MultiResult) result;
            return this.resolveMultipleResults(query, multiResult); // Recursive call.
        }

        return result;
    }

    public @NotNull DownloadResult downloadJar(@NotNull InstallProgress progress, @NotNull SuccessResult resolveResult)
    {
        UUID downloadID = progress.getInstallActionID();

        DownloadStartedSignal downloadingSignal = new DownloadStartedSignal(
                downloadID,
                progress.getInstallTempDir().resolve(progress.getInstallActionID().toString() + ".jar"),
                resolveResult.getDownloadUrl()
        );

        this.signalHandler.handleSignal(downloadingSignal);  // SignalHandler can change the download URL and download path.

        Path path = downloadingSignal.getDownloadPath();
        String url = downloadingSignal.getUrl();

        try
        {
            long size = Requests.downloadFile(RequestMethod.GET, url, path, new DownloadProgressConsumer(downloadID));

            this.signalHandler.handleSignal(new DownloadSucceedSignal(downloadID, path, size));

            return new DownloadResult(path, true, size, null); // downloadFailedReason is null.
        }
        catch (IOException e)
        {
            DownloadErrorCause cause;
            Object signalValue;

            if (e.getMessage().startsWith("HTTP error "))
            {
                cause = DownloadErrorCause.ILLEGAL_HTTP_RESPONSE;
                signalValue = e.getMessage().substring(11);
            }
            else if (e.getMessage().startsWith("No response body was returned"))
            {
                cause = DownloadErrorCause.NO_BODY_IN_RESPONSE;
                signalValue = null;
            }
            else
            {
                cause = DownloadErrorCause.IO_EXCEPTION;
                signalValue = e;
            }

            DownloadErrorSignal error = new DownloadErrorSignal(downloadID, cause, signalValue);
            this.signalHandler.handleSignal(error);

            return new DownloadResult(null, false, -1, cause.toFailedReason());
        }
        catch (Exception e)
        {
            this.signalHandler.handleSignal(new DownloadErrorSignal(downloadID, DownloadErrorCause.UNKNOWN_ERROR, e));

            return new DownloadResult(null, false, -1, DownloadErrorCause.UNKNOWN_ERROR.toFailedReason());
        }
    }

    public @Nullable ResolveResult normalizeResolveResult(@NotNull String query, @NotNull ResolveResult queryResolveResult)
    {
        if (queryResolveResult instanceof ErrorResult)
        {
            this.signalHandler.handleSignal(new PluginResolveErrorSignal((ErrorResult) queryResolveResult));
            return null;
        }
        else if (queryResolveResult instanceof MultiResult)
        {
            this.progress.setPhase(InstallPhase.MULTIPLE_RESULT_RESOLVING);

            MultiResult multiResult = (MultiResult) queryResolveResult;
            ResolveResult actualResolveResult = this.resolveMultipleResults(query, multiResult);

            if (actualResolveResult instanceof ErrorResult)
            {
                // MultiResult has been resolved, but the actual result is an error
                this.signalHandler.handleSignal(new PluginResolveErrorSignal((ErrorResult) actualResolveResult));
                return null;
            }

            // MultiResult has been resolved, and the actual result is a SuccessResult
            // (resolveMultipleResults() should not return a MultiResult)

            this.progress.setPhase(InstallPhase.QUERY_RESOLVING);  // Reset to QUERY_RESOLVING from MULTIPLE_RESULT_RESOLVING

            return actualResolveResult;
        }
        else
            return queryResolveResult;
    }

    public @Nullable InstallResult checkResolveError(@Nullable ResolveResult resolveResult)
    {
        if (resolveResult == null)  // normalizeResolveResult() returns null if the ResolveResult is error.
            return InstallResult.error(progress, FailedReason.GOT_ERROR_RESULT);
        if (!(resolveResult instanceof SuccessResult)) // Should never happen
            return InstallResult.error(progress, FailedReason.ILLEGAL_INTERNAL_STATE);

        return null;
    }

    @Value
    public static class DownloadResult
    {
        Path path;
        boolean success;
        long totalSize;

        FailedReason downloadFailedReason;
    }

    @Value
    private class DownloadProgressConsumer implements Consumer<DownloadProgress>
    {
        UUID installID;

        @Override
        public void accept(DownloadProgress downloadProgress)
        {
            PlumbingInstaller.this.signalHandler.handleSignal(new DownloadProgressSignal(
                    installID,
                    downloadProgress.getTotalSize(),
                    downloadProgress.getDownloaded(),
                    downloadProgress.getPercentage()
            ));
        }
    }
}
