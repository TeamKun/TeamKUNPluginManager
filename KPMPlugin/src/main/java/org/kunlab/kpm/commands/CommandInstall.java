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
import org.kunlab.kpm.installer.impls.install.InstallArgument;
import org.kunlab.kpm.lang.LangProvider;

import java.util.List;

@AllArgsConstructor
public class CommandInstall extends CommandBase
{
    private final TeamKunPluginManager plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        String query = args[0];

        Runner.runAsync(() ->
                this.plugin.getHeadInstallers().runInstall(terminal, InstallArgument.builder(query).build())
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
        return "kpm.install";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return LangProvider.getComponent("command.install");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("query", "string")
        };
    }
}
