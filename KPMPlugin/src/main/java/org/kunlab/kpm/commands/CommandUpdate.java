package org.kunlab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.TeamKunPluginManager;
import org.kunlab.kpm.installer.impls.update.UpdateArgument;
import org.kunlab.kpm.interfaces.KPMRegistry;

import java.util.List;

@AllArgsConstructor
public class CommandUpdate extends CommandBase
{
    private final TeamKunPluginManager plugin;
    private final KPMRegistry registry;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        Runner.runAsync(() ->
                this.plugin.getHeadInstallers().runUpdate(terminal, new UpdateArgument(
                        this.registry.getEnvironment().getSources()
                ))
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
        return "kpm.update";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return (TextComponent) LangProvider.getComponent("command.update");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
