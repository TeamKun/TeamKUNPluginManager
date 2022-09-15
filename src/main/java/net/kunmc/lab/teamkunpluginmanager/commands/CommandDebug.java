package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.command.SubCommandWith;
import net.kunmc.lab.teamkunpluginmanager.commands.debug.CommandInstallDebug;
import net.kunmc.lab.teamkunpluginmanager.commands.debug.CommandUninstallDebug;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CommandDebug extends SubCommandWith
{
    private static final HashMap<String, CommandBase> COMMANDS;

    static
    {
        COMMANDS = new HashMap<>();
        COMMANDS.put("installDebug", new CommandInstallDebug());
        COMMANDS.put("uninstallDebug", new CommandUninstallDebug());
    }

    @Override
    protected String getName()
    {
        return "debug";
    }

    @Override
    protected Map<String, CommandBase> getSubCommands(@NotNull CommandSender sender)
    {
        return COMMANDS;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.debug";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("KPMのデバッグに使用します。");
    }
}
