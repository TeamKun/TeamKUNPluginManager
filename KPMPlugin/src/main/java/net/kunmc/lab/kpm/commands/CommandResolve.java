package net.kunmc.lab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class CommandResolve extends CommandBase
{
    private final KPMDaemon daemon;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1, 1))
            return;

        String query = args[0];

        terminal.info("プラグインの名前解決をしています…");
        ResolveResult result = this.daemon.getPluginResolver().resolve(query);

        ResolveResultWriter writer = new ResolveResultWriter(terminal, result);
        terminal.info("結果を出力しています…");
        writer.write();
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
