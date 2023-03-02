package org.kunlab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.TeamKunPluginManager;
import org.kunlab.kpm.installer.impls.upgrade.UpgradeArgument;
import org.kunlab.kpm.lang.LangProvider;

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
        return LangProvider.getComponent("command.upgrade");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                optional("query", "string")
        };
    }
}
