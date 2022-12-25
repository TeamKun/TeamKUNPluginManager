package net.kunmc.lab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.TeamKunPluginManager;
import net.kunmc.lab.kpm.installer.impls.upgrade.UpgradeArgument;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class CommandUpgrade extends CommandBase
{
    private final TeamKunPluginManager plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 0))
            return;

        List<String> targets = null;
        if (args.length == 1)
            targets = new ArrayList<>(Arrays.asList(args));

        UpgradeArgument argument = new UpgradeArgument(targets);
        Runner.runAsync(() ->
                this.plugin.getHeadInstallers().runUpgrade(terminal, argument)
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
        return "kpm.upgrade";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("プラグインをアップグレードします。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                optional("query", "string")
        };
    }
}
