package net.kunmc.lab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.TeamKunPluginManager;
import net.kunmc.lab.kpm.installer.impls.register.RegisterArgument;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class CommandRegister extends CommandBase
{
    private final TeamKunPluginManager plugin;
    private final KPMDaemon daemon;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (args.length < 1 && sender instanceof BlockCommandSender)
        {
            terminal.error("コマンドブロックから実行するには第一引数が必須です。");
            return;
        }

        String tokenOrNull = args.length >= 1 ? args[0]: null;

        Runner.runAsync(() ->
                this.plugin.getHeadInstallers().runRegister(terminal, new RegisterArgument(tokenOrNull))
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.register";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("事前に取得したトークンを設定または、GitHubにログインしてトークンを設定します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                optional("token", "string")
        };
    }
}
