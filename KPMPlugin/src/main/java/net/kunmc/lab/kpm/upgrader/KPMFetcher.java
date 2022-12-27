package net.kunmc.lab.kpm.upgrader;

import lombok.experimental.UtilityClass;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.resolver.PluginResolver;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.MultiResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.utils.versioning.Version;

@UtilityClass
public class KPMFetcher
{
    private static final String KPM_REPO_OWNER = "TeamKun";
    private static final String KPM_REPO_NAME = "TeamKunPluginManager";
    private static final String KPM_REPO_URL = String.format("https://github.com/%s/%s", KPM_REPO_OWNER, KPM_REPO_NAME);

    public static Version fetchLatestKPMVersion(KPMDaemon daemon)
    {
        PluginResolver resolver = daemon.getPluginResolver();
        ResolveResult resolveResult = resolver.resolve("github>" + KPM_REPO_URL);

        if (resolveResult instanceof MultiResult)
            resolveResult = resolveResult.getResolver().autoPickOnePlugin((MultiResult) resolveResult);
        if (resolveResult instanceof ErrorResult)
        {
            ErrorResult errorResult = (ErrorResult) resolveResult;
            throw new IllegalStateException("Unable to fetch latest KPM version: " + errorResult.getMessage());
        }

        assert resolveResult instanceof SuccessResult;

        SuccessResult successResult = (SuccessResult) resolveResult;
        String versionString = successResult.getVersion();
        if (versionString == null)
            throw new IllegalStateException("Unable to fetch latest KPM version: No version is defined: " + successResult.getFileName());
        else if (!Version.isValidVersionString(versionString))
            throw new IllegalStateException("Unable to fetch latest KPM version: Malformed version string: " + versionString);

        return Version.of(versionString);
    }
}
