package net.kunmc.lab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.KPMRegistry;
import net.kunmc.lab.kpm.TeamKunPluginManager;
import net.kunmc.lab.kpm.installer.impls.update.UpdateArgument;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return of("エイリアスをアップデートします。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
