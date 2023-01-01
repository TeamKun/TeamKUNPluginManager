package net.kunmc.lab.kpm.commands;

import net.kunmc.lab.kpm.KPMRegistry;
import net.kunmc.lab.kpm.commands.debug.CommandDepTreeDebug;
import net.kunmc.lab.kpm.commands.debug.CommandInstallDebug;
import net.kunmc.lab.kpm.commands.debug.CommandUninstallDebug;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.command.SubCommandWith;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CommandDebug extends SubCommandWith
{
    private final HashMap<String, CommandBase> commands;

    public CommandDebug(KPMRegistry registry)
    {
        this.commands = new HashMap<>();


        this.commands.put("installDebug", new CommandInstallDebug(registry));
        this.commands.put("uninstallDebug", new CommandUninstallDebug(registry));
        this.commands.put("depTree", new CommandDepTreeDebug(registry.getPluginMetaManager()));
    }

    @Override
    protected String getName()
    {
        return "debug";
    }

    @Override
    protected Map<String, CommandBase> getSubCommands(@NotNull CommandSender sender)
    {
        return this.commands;
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