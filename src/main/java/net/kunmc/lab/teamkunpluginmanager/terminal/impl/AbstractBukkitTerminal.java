package net.kunmc.lab.teamkunpluginmanager.terminal.impl;

import net.kunmc.lab.teamkunpluginmanager.terminal.framework.Terminal;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBukkitTerminal implements Terminal
{
    private final Audience audience;

    public AbstractBukkitTerminal(Audience audience)
    {
        this.audience = audience;
    }

    @Override
    public void info(@NotNull String message)
    {
        writeLine(ChatColor.BLUE + "I: " + message);
    }

    @Override
    public void error(@NotNull String message)
    {
        writeLine(ChatColor.RED + "E: " + message);
    }

    @Override
    public void success(@NotNull String message)
    {
        writeLine(ChatColor.GREEN + "S: " + message);
    }

    @Override
    public void warn(@NotNull String message)
    {
        writeLine(ChatColor.YELLOW + "W: " + message);
    }

    @Override
    public void writeLine(@NotNull String message)
    {
        write(Component.text(message));
    }

    @Override
    public void write(@NotNull Component component)
    {
        audience.sendMessage(component);
    }
}
