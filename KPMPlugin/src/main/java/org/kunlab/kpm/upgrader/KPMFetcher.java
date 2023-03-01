package org.kunlab.kpm.upgrader;

import lombok.experimental.UtilityClass;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.resolver.interfaces.PluginResolver;
import org.kunlab.kpm.resolver.interfaces.result.ErrorResult;
import org.kunlab.kpm.resolver.interfaces.result.MultiResult;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;
import org.kunlab.kpm.resolver.interfaces.result.SuccessResult;
import org.kunlab.kpm.resolver.result.AbstractSuccessResult;
import org.kunlab.kpm.resolver.result.ErrorResultImpl;
import org.kunlab.kpm.resolver.result.MultiResultImpl;
import org.kunlab.kpm.versioning.Version;

@UtilityClass
public class KPMFetcher
{
    private static final String KPM_REPO_OWNER = "TeamKun";
    private static final String KPM_REPO_NAME = "TeamKunPluginManager";
    private static final String KPM_REPO_URL = String.format("https://github.com/%s/%s", KPM_REPO_OWNER, KPM_REPO_NAME);

    private static final String UPGRADER_REPOSITORY = "TeamKUN/KPMUpgrader";

    public static Version fetchLatestKPMVersion(KPMRegistry registry)
    {
        PluginResolver resolver = registry.getPluginResolver();
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

    public static String fetchUpgraderJarFile(KPMRegistry registry)
    {
        String upgraderQuery = "github>" + UPGRADER_REPOSITORY;

        ResolveResult result = registry.getPluginResolver().resolve(upgraderQuery);

        if (result instanceof MultiResult)
            result = result.getResolver().autoPickOnePlugin((MultiResultImpl) result);

        if (result instanceof ErrorResult)
            throw new IllegalStateException("Unable to fetch upgrader jar file: " + ((ErrorResult) result).getMessage());

        assert result instanceof SuccessResult;

        return ((SuccessResult) result).getDownloadUrl();
    }
}
