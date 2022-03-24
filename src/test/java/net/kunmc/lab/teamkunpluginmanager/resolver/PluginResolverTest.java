package net.kunmc.lab.teamkunpluginmanager.resolver;

import net.kunmc.lab.teamkunpluginmanager.resolver.impl.GitHubSuccessResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.GitHubURLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.OmittedGitHubResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.impl.SpigotMCResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.SuccessResult;
import org.junit.Ignore;
import org.junit.Test;

public class PluginResolverTest
{
    public static void printResult(ResolveResult result, boolean first)
    {
        if (result instanceof SuccessResult)
        {
            SuccessResult successResult = (SuccessResult) result;
            System.out.println(successResult.getFileName() + ":" + successResult.getDownloadUrl() + " @" + successResult.getVersion());

            if (result instanceof GitHubSuccessResult)
            {
                GitHubSuccessResult gitHubSuccessResult = (GitHubSuccessResult) result;
                System.out.println("GitHub: " + gitHubSuccessResult.getOwner() + "/" + gitHubSuccessResult.getRepoName());
                System.out.println("GitHub: " + gitHubSuccessResult.getTitle() + ": " + gitHubSuccessResult.getReleaseName());
                System.out.println("GitHub: " + gitHubSuccessResult.getUrl());
            }
        }
        else if (result instanceof ErrorResult)
        {
            ErrorResult errorResult = (ErrorResult) result;
            System.out.println(errorResult.getCause());
            System.out.println(errorResult.getCause().getMessage());
            System.out.println(errorResult.getSource());
        }
        else if (result instanceof MultiResult)
        {
            MultiResult multiResult = (MultiResult) result;
            for (ResolveResult resolveResult : multiResult.getResults())
                printResult(resolveResult, false);
        }
    }

    @Ignore
    @Test
    public void resolveTest()
    {

        PluginResolver resolver = new PluginResolver();
        resolver.addResolver(new OmittedGitHubResolver(), "github", "gh");
        resolver.addResolver(new GitHubURLResolver(), "github", "gh");
        resolver.addResolver(new SpigotMCResolver(), "spigotmc", "spigot", "spiget");
        // resolver.addResolver(new CurseBukkitResolver(), "curseforge", "curse", "forge", "bukkit");

        // resolver.addResolver(new KnownPluginsResolver(), "local", "alias");


        ResolveResult result;
        // result = resolver.resolve("https://www.spigotmc.org/resources/coreprotect.8631/");
        // printResult(result, true);

        // result = resolver.resolve("spigot>https://github.com/TeamKUN/TeamKUNPluginManager==v1.0");
        //result = resolver.resolve("TeamKUN/TeamKUNPluginManager==v1.0");
        result = resolver.resolve("bukkit>coreprotect==v1.0");

        printResult(result, true);
    }
}
