package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.command.SubCommandWith;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CommandDebug extends SubCommandWith
{
    @Override
    protected String getName()
    {
        return "debug";
    }

    @Override
    protected Map<String, CommandBase> getSubCommands(@NotNull CommandSender sender)
    {
        return null;
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
