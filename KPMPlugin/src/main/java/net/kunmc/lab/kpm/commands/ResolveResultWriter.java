package net.kunmc.lab.kpm.commands;

import net.kunmc.lab.kpm.interfaces.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.MarketplaceResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.MultiResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.impl.CurseBukkitSuccessResult;
import net.kunmc.lab.kpm.resolver.impl.GitHubSuccessResult;
import net.kunmc.lab.kpm.resolver.impl.SpigotMCSuccessResult;
import net.kunmc.lab.kpm.resolver.result.ErrorResultImpl;
import net.kunmc.lab.kpm.resolver.result.MultiResultImpl;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.utils.TerminalWriter;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import org.bukkit.ChatColor;

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

        this.printString("リゾルバ", result.getResolver().getClass().getSimpleName());

        if (result instanceof ErrorResultImpl)
        {
            ErrorResult errorResult = (ErrorResult) result;
            this.printStringFull(ChatColor.RED + "エラー", ChatColor.RED + errorResult.getMessage() + " (" + errorResult.getCause() + ")");
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
            this.printBoolean("複数", true);

            ResolveResult[] results = multiResult.getResults();

            for (int i = results.length - 1; i >= 0; i--)
                this.writeRecursive(results[i], true);
        }
    }

    private void printSuccessResult(SuccessResult result)
    {
        this.printStringOrEmpty("ファイル名", result.getFileName());
        this.printStringOrEmpty("バージョン", result.getVersion());
        this.printStringFull("ダウンロード", result.getDownloadUrl());
    }

    private void printAdditionalInformation(ResolveResult result)
    {
        if (result instanceof CurseBukkitSuccessResult)
        {
            CurseBukkitSuccessResult curseBukkitSuccessResult = (CurseBukkitSuccessResult) result;

            this.printString("種類", "CURSE_FORGE");
            this.printMarketplaceResult(curseBukkitSuccessResult);
            this.printStringOrEmpty("バージョン", curseBukkitSuccessResult.getVersion());
            this.printString("ID", curseBukkitSuccessResult.getSlug() + "#" + curseBukkitSuccessResult.getSlug());
            this.printString("説明", curseBukkitSuccessResult.getDescription());
            this.printSeparatorShort();
        }
        else if (result instanceof GitHubSuccessResult)
        {
            GitHubSuccessResult githubSuccessResult = (GitHubSuccessResult) result;

            this.printString("種類", "GITHUB");
            this.printString("リポジトリ", "https://github.com/" +
                    githubSuccessResult.getOwner() + "/" + githubSuccessResult.getRepoName());
            this.printString("ファイルサイズ", String.valueOf(githubSuccessResult.getSize()));
            this.printString("リリース名", githubSuccessResult.getReleaseName());
            this.printString("リリースID", String.valueOf(githubSuccessResult.getReleaseId()));
            this.printString("リリース内容", githubSuccessResult.getReleaseBody());
            this.printSeparatorShort();
        }
        else if (result instanceof SpigotMCSuccessResult)
        {
            SpigotMCSuccessResult spigotMCSuccessResult = (SpigotMCSuccessResult) result;

            this.printString("種類", "SPIGOT_MC");
            this.printMarketplaceResult(spigotMCSuccessResult);
            this.printStringFull("対応バージョン", String.join(", ", spigotMCSuccessResult.getVersions()));
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
        this.printString("タイトル", result.getTitle());
        this.printString("説明", result.getDescription());
        this.printStringFull("公開先", result.getUrl());

    }
}
