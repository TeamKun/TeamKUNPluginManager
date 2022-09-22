package net.kunmc.lab.teamkunpluginmanager.commands.debug;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.command.SubCommandWith;
import net.kunmc.lab.teamkunpluginmanager.commands.debug.deptree.CommandMarkAsDependencyCommand;
import net.kunmc.lab.teamkunpluginmanager.plugin.meta.PluginMetaManager;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CommandDepTreeDebug extends SubCommandWith
{
    private final HashMap<String, CommandBase> COMMANDS;

    public CommandDepTreeDebug(PluginMetaManager manager)
    {
        COMMANDS = new HashMap<>();

        COMMANDS.put("markAsDependency", new CommandMarkAsDependencyCommand(manager.getProvider()));
    }

    @Override
    protected String getName()
    {
        return "depTree";
    }

    @Override
    protected Map<String, CommandBase> getSubCommands(@NotNull CommandSender sender)
    {
        return COMMANDS;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.debug.deptree";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("依存関係ツリーのデバッグコマンドです。");
    }
}
