package net.kunmc.lab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.resolver.impl.CurseBukkitSuccessResult;
import net.kunmc.lab.kpm.resolver.impl.GitHubSuccessResult;
import net.kunmc.lab.kpm.resolver.impl.SpigotMCSuccessResult;
import net.kunmc.lab.kpm.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.resolver.result.MarketplaceResult;
import net.kunmc.lab.kpm.resolver.result.MultiResult;
import net.kunmc.lab.kpm.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.resolver.result.SuccessResult;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class CommandResolve extends CommandBase
{
    private static final String DELIMITER_SMALL;

    private static final String DELIMITER;

    static
    {
        DELIMITER_SMALL = ChatColor.BLUE + "==============================";
        DELIMITER = StringUtils.repeat(DELIMITER_SMALL, 2);
    }

    @NotNull
    private final KPMDaemon daemon;

    private static void printResult(Terminal terminal, ResolveResult result, boolean smallDelimiter)
    {
        if (smallDelimiter)
            terminal.info(DELIMITER_SMALL);
        else
            terminal.info(DELIMITER);

        terminal.info("使用されたリザルバ：" + result.getResolver().getClass().getSimpleName());

        if (result instanceof ErrorResult)
        {
            ErrorResult errorResult = (ErrorResult) result;
            terminal.error("プラグインの名前解決に失敗しました：" + errorResult.getCause());
        }
        else if (result instanceof SuccessResult)
        {
            SuccessResult successResult = (SuccessResult) result;
            terminal.info("ファイル名：" + successResult.getFileName());
            terminal.info("バージョン：" + successResult.getVersion());
            terminal.info("ダウンロード：" + successResult.getDownloadUrl());

            printAdditionalInformation(terminal, result);
        }
        else if (result instanceof MultiResult)
        {
            MultiResult multiResult = (MultiResult) result;
            terminal.info("複数のファイルが見つかりました。");
            for (ResolveResult subResult : multiResult.getResults())
                printResult(terminal, subResult, true);
        }
    }

    private static void printAdditionalInformation(Terminal terminal, ResolveResult result)
    {
        if (result instanceof CurseBukkitSuccessResult)
        {
            CurseBukkitSuccessResult curseBukkitSuccessResult = (CurseBukkitSuccessResult) result;

            terminal.info("[CURSE_FORGE]");
            terminal.info("バージョン：" + curseBukkitSuccessResult.getVersion());
            terminal.info("ID：" + curseBukkitSuccessResult.getSlug() + "#" + curseBukkitSuccessResult.getSlug());
            terminal.info("説明：" + curseBukkitSuccessResult.getDescription());
            terminal.writeLine(DELIMITER_SMALL);
        }

        if (result instanceof GitHubSuccessResult)
        {
            GitHubSuccessResult githubSuccessResult = (GitHubSuccessResult) result;

            terminal.info("[GITHUB]");
            terminal.info("リポジトリ：" + githubSuccessResult.getOwner() + "/" + githubSuccessResult.getRepoName());
            terminal.info("ファイルサイズ：" + githubSuccessResult.getSize());
            terminal.info("リリース名：" + githubSuccessResult.getReleaseName());
            terminal.info("リリースID：" + githubSuccessResult.getReleaseId());
            terminal.info("リリース内容：" + githubSuccessResult.getReleaseBody());
            terminal.writeLine(DELIMITER_SMALL);
        }

        if (result instanceof SpigotMCSuccessResult)
        {
            SpigotMCSuccessResult spigotMCSuccessResult = (SpigotMCSuccessResult) result;

            terminal.info("[SPIGOT_MC]");
            terminal.info("バージョン：" + String.join(", ", spigotMCSuccessResult.getVersions()));
            terminal.writeLine(DELIMITER_SMALL);
        }

        if (result instanceof MarketplaceResult)
        {
            MarketplaceResult marketplaceResult = (MarketplaceResult) result;
            terminal.info("[MARKETPLACE]");
            terminal.info("タイトル：" + marketplaceResult.getTitle());
            terminal.info("説明：" + marketplaceResult.getDescription());
            terminal.info("公開先：" + marketplaceResult.getUrl());
            terminal.writeLine(DELIMITER_SMALL);
        }
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1, 1))
            return;

        String query = args[0];

        terminal.info("プラグインの名前解決をしています...");
        ResolveResult result = this.daemon.getPluginResolver().resolve(query);

        printResult(terminal, result, false);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.resolve";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("プラグインを名前解決します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("query", "string")
        };
    }
}
