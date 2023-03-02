package org.kunlab.kpm.commands;

import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;
import org.kunlab.kpm.resolver.impl.CurseBukkitSuccessResult;
import org.kunlab.kpm.resolver.impl.GitHubSuccessResult;
import org.kunlab.kpm.resolver.impl.SpigotMCSuccessResult;
import org.kunlab.kpm.resolver.interfaces.result.ErrorResult;
import org.kunlab.kpm.resolver.interfaces.result.MarketplaceResult;
import org.kunlab.kpm.resolver.interfaces.result.MultiResult;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;
import org.kunlab.kpm.resolver.interfaces.result.SuccessResult;
import org.kunlab.kpm.resolver.result.ErrorResultImpl;
import org.kunlab.kpm.resolver.result.MultiResultImpl;
import org.kunlab.kpm.utils.TerminalWriter;

public class ResolveResultWriter extends TerminalWriter
{
    private final ResolveResult result;

    public ResolveResultWriter(Terminal terminal, ResolveResult result)
    {
        super(terminal);
        this.result = result;
    }

    @Override
    public void write()
    {
        this.writeRecursive(this.result, false);
    }

    private void writeRecursive(ResolveResult result, boolean isOnRecursive)
    {
        if (isOnRecursive)
            this.printSeparatorShort();
        else
            this.printSeparator();

        this.printString("command.resolve.resolver", result.getResolver().getClass().getSimpleName());

        if (result instanceof ErrorResultImpl)
        {
            ErrorResult errorResult = (ErrorResult) result;
            this.printStringFull(
                    "general.error",
                    ChatColor.RED + errorResult.getMessage() + " (" + errorResult.getCause() + ")"
            );
        }
        else if (result instanceof SuccessResult)
        {
            SuccessResult successResult = (SuccessResult) result;

            this.printSuccessResult(successResult);
            this.printSeparatorShort();
            this.printAdditionalInformation(result);
        }
        else if (result instanceof MultiResultImpl)
        {
            MultiResult multiResult = (MultiResult) result;
            this.printBoolean("command.resolve.multi", true);

            ResolveResult[] results = multiResult.getResults();

            for (int i = results.length - 1; i >= 0; i--)
                this.writeRecursive(results[i], true);
        }
    }

    private void printSuccessResult(SuccessResult result)
    {
        this.printStringOrEmpty("command.resolve.file_name", result.getFileName());
        this.printStringOrEmpty("command.resolve.version", result.getVersion());
        this.printStringFull("command.resolve.download_url", result.getDownloadUrl());
    }

    private void printAdditionalInformation(ResolveResult result)
    {
        if (result instanceof CurseBukkitSuccessResult)
        {
            CurseBukkitSuccessResult curseBukkitSuccessResult = (CurseBukkitSuccessResult) result;

            this.printString("command.resolve.extra.type", "CURSE_FORGE");
            this.printMarketplaceResult(curseBukkitSuccessResult);
            this.printStringOrEmpty("command.resolve.version", curseBukkitSuccessResult.getVersion());
            this.printString("command.resolve.extra.id", curseBukkitSuccessResult.getSlug() + "#" + curseBukkitSuccessResult.getSlug());
            this.printString("command.resolve.extra.description", curseBukkitSuccessResult.getDescription());
            this.printSeparatorShort();
        }
        else if (result instanceof GitHubSuccessResult)
        {
            GitHubSuccessResult githubSuccessResult = (GitHubSuccessResult) result;

            this.printString("command.resolve.extra.type", "GITHUB");
            this.printString("command.resolve.extra.gh.repo", "https://github.com/" +
                    githubSuccessResult.getOwner() + "/" + githubSuccessResult.getRepoName());
            this.printString("command.resolve.extra.gh.file_size", String.valueOf(githubSuccessResult.getSize()));
            this.printString("command.resolve.extra.gh.release_name", githubSuccessResult.getReleaseName());
            this.printString("command.resolve.extra.id", String.valueOf(githubSuccessResult.getReleaseId()));
            this.printString("command.resolve.extra.gh.release_body", githubSuccessResult.getReleaseBody());
            this.printSeparatorShort();
        }
        else if (result instanceof SpigotMCSuccessResult)
        {
            SpigotMCSuccessResult spigotMCSuccessResult = (SpigotMCSuccessResult) result;

            this.printString("command.resolve.extra.type", "SPIGOT_MC");
            this.printMarketplaceResult(spigotMCSuccessResult);
            this.printStringFull("command.resolve.extra.spigot.compatible_versions", String.join(", ", spigotMCSuccessResult.getVersions()));
            this.printSeparatorShort();
        }
        else if (result instanceof MarketplaceResult)
        {
            MarketplaceResult marketplaceResult = (MarketplaceResult) result;

            this.printMarketplaceResult(marketplaceResult);
            this.printSeparatorShort();
        }
    }

    private void printMarketplaceResult(MarketplaceResult result)
    {
        this.printString("command.resolve.extra.title", result.getTitle());
        this.printString("command.resolve.extra.description", result.getDescription());
        this.printStringFull("command.resolve.extra.publish_address", result.getUrl());

    }
}
