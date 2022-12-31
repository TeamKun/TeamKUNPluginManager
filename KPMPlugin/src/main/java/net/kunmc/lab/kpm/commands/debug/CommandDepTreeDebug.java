package net.kunmc.lab.kpm.commands.debug;

import net.kunmc.lab.kpm.commands.debug.deptree.CommandMarkAsDependency;
import net.kunmc.lab.kpm.commands.debug.deptree.CommandPurge;
import net.kunmc.lab.kpm.commands.debug.deptree.CommandRelation;
import net.kunmc.lab.kpm.interfaces.meta.PluginMetaManager;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.command.SubCommandWith;
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
        this.COMMANDS = new HashMap<>();

        this.COMMANDS.put("markAsDependency", new CommandMarkAsDependency(manager.getProvider()));
        this.COMMANDS.put("relation", new CommandRelation(manager.getProvider()));
        this.COMMANDS.put("purge", new CommandPurge(manager.getProvider()));
    }

    @Override
    protected String getName()
    {
        return "depTree";
    }

    @Override
    protected Map<String, CommandBase> getSubCommands(@NotNull CommandSender sender)
    {
        return this.COMMANDS;
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
