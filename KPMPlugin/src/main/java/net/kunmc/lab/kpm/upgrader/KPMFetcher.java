package net.kunmc.lab.kpm.upgrader;

import lombok.experimental.UtilityClass;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.interfaces.resolver.PluginResolver;
import net.kunmc.lab.kpm.interfaces.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.resolver.result.AbstractSuccessResult;
import net.kunmc.lab.kpm.resolver.result.ErrorResultImpl;
import net.kunmc.lab.kpm.resolver.result.MultiResultImpl;
import net.kunmc.lab.kpm.versioning.Version;

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

        if (resolveResult instanceof MultiResultImpl)
            resolveResult = resolveResult.getResolver().autoPickOnePlugin((MultiResultImpl) resolveResult);
        if (resolveResult instanceof ErrorResultImpl)
        {
            ErrorResult errorResult = (ErrorResult) resolveResult;
            throw new IllegalStateException("Unable to fetch latest KPM version: " + errorResult.getMessage());
        }

        assert resolveResult instanceof AbstractSuccessResult;

        SuccessResult successResult = (SuccessResult) resolveResult;
        String versionString = successResult.getVersion();
        if (versionString == null)
            throw new IllegalStateException("Unable to fetch latest KPM version: No version is defined: " + successResult.getFileName());
        else if (!Version.isValidVersionString(versionString))
            throw new IllegalStateException("Unable to fetch latest KPM version: Malformed version string: " + versionString);

        return Version.of(versionString);
    }
}
