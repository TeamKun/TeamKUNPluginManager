package net.kunmc.lab.kpm.commands;

import net.kunmc.lab.kpm.resolver.impl.CurseBukkitSuccessResult;
import net.kunmc.lab.kpm.resolver.impl.GitHubSuccessResult;
import net.kunmc.lab.kpm.resolver.impl.SpigotMCSuccessResult;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.MarketplaceResult;
import net.kunmc.lab.kpm.resolver.result.MultiResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
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

        if (result instanceof ErrorResult)
        {
            ErrorResult errorResult = (ErrorResult) result;
            this.printString(ChatColor.RED + "エラー", errorResult.getMessage() + " (" + errorResult.getCause() + ")");
        }
        else if (result instanceof SuccessResult)
        {
            SuccessResult successResult = (SuccessResult) result;

            this.printSuccessResult(successResult);
            this.printSeparatorShort();
            this.printAdditionalInformation(result);
        }
        else if (result instanceof MultiResult)
        {
            MultiResult multiResult = (MultiResult) result;
            this.printBoolean("複数", true);

            for (ResolveResult subResult : multiResult.getResults())
                this.writeRecursive(subResult, true);
        }
    }

    private void printSuccessResult(SuccessResult result)
    {
        this.printStringOrEmpty("ファイル名", result.getFileName());
        this.printStringOrEmpty("バージョン", result.getVersion());
        this.printString("ダウンロード", result.getDownloadUrl());
    }

    private void printAdditionalInformation(ResolveResult result)
    {
        if (result instanceof CurseBukkitSuccessResult)
        {
            CurseBukkitSuccessResult curseBukkitSuccessResult = (CurseBukkitSuccessResult) result;

            this.printString("種類", "CURSE_FORGE");
            this.printStringOrEmpty("バージョン", curseBukkitSuccessResult.getVersion());
            this.printString("ID", curseBukkitSuccessResult.getSlug() + "#" + curseBukkitSuccessResult.getSlug());
            this.printString("説明", curseBukkitSuccessResult.getDescription());
            this.printSeparatorShort();
        }

        if (result instanceof GitHubSuccessResult)
        {
            GitHubSuccessResult githubSuccessResult = (GitHubSuccessResult) result;

            this.printString("種類", "GITHUB");
            this.printString("リポジトリ", githubSuccessResult.getOwner() + "/" + githubSuccessResult.getRepoName());
            this.printString("ファイルサイズ", String.valueOf(githubSuccessResult.getSize()));
            this.printString("リリース名", githubSuccessResult.getReleaseName());
            this.printString("リリースID", String.valueOf(githubSuccessResult.getReleaseId()));
            this.printString("リリース内容", githubSuccessResult.getReleaseBody());
            this.printSeparatorShort();
        }

        if (result instanceof SpigotMCSuccessResult)
        {
            SpigotMCSuccessResult spigotMCSuccessResult = (SpigotMCSuccessResult) result;

            this.printString("種類", "SPIGOT_MC");
            this.printStringFull("対応バージョン", String.join(", ", spigotMCSuccessResult.getVersions()));
            this.printSeparatorShort();
        }

        if (result instanceof MarketplaceResult)
        {
            MarketplaceResult marketplaceResult = (MarketplaceResult) result;

            this.printString("種類", "MARKETPLACE");
            this.printString("タイトル", marketplaceResult.getTitle());
            this.printString("説明", marketplaceResult.getDescription());
            this.printString("公開先", marketplaceResult.getUrl());
            this.printSeparatorShort();
        }
    }
}
