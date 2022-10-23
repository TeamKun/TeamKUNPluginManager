package net.kunmc.lab.kpm.commands;

import net.kunmc.lab.kpm.KPMDaemon;
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
    private static final HashMap<String, CommandBase> COMMANDS;

    static
    {
        KPMDaemon daemon = KPMDaemon.getInstance();

        COMMANDS = new HashMap<>();
        COMMANDS.put("installDebug", new CommandInstallDebug(daemon));
        COMMANDS.put("uninstallDebug", new CommandUninstallDebug(daemon));
        COMMANDS.put("depTree", new CommandDepTreeDebug(daemon.getPluginMetaManager()));
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
