package net.kunmc.lab.plugin.kpmupgrader;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.KPMEnvironment;
import net.kunmc.lab.kpm.http.Requests;
import net.kunmc.lab.kpm.resolver.impl.github.GitHubURLResolver;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class KPMDaemonMock extends KPMDaemon
{
    public KPMDaemonMock(@NotNull KPMEnvironment env)
    {
        super(env);
    }

    @Override
    public void setupDaemon(@NotNull List<String> organizationNames)
    {
        this.getPluginResolver().addResolver(new GitHubURLResolver(), "$");
        Requests.setVersion("KPMUpdater");
        Requests.setTokenStore(this.getTokenStore());

        try
        {
            if (!this.getTokenStore().loadToken())
            {
                String tokenEnv = System.getenv("TOKEN");

                if (tokenEnv == null || tokenEnv.isEmpty())
                    throw new IllegalStateException("Token is not set.");

                this.getTokenStore().fromEnv();
            }
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
