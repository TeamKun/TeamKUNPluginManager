package net.kunmc.lab.teamkunpluginmanager.terminal.impl;

import net.kunmc.lab.teamkunpluginmanager.terminal.framework.Terminal;
import net.kunmc.lab.teamkunpluginmanager.terminal.impl.player.PlayerTerminal;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Terminals
{
    private static final Map<UUID, Terminal> terminals;

    static
    {
        terminals = new HashMap<>();
    }

    private Terminals()
    {
    }

    public static @NotNull Terminal of(@NotNull Player player)
    {
        Terminal terminal = terminals.get(player.getUniqueId());
        if (terminal != null)
            return terminal;

        terminal = new PlayerTerminal(player);
        terminals.put(player.getUniqueId(), terminal);
        return terminal;
    }
}
