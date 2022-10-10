package net.kunmc.lab.teamkunpluginmanager.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.installer.impls.clean.CleanArgument;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class CommandClean extends CommandBase
{
    private final TeamKunPluginManager plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        Runner.runAsync(() ->
                this.plugin.getHeadInstallers().runGarbageClean(terminal, new CleanArgument())
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
        return "kpm.clean";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("削除されて残ったプラグインの不要データを自動削除します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
