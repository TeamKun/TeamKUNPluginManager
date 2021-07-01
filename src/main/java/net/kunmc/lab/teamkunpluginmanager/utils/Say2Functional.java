package net.kunmc.lab.teamkunpluginmanager.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Say2Functional implements Listener
{

    private final HashMap<UUID, FunctionalEntry> say2func;
    private FunctionalEntry consoleFunc;

    public Say2Functional(Plugin plugin)
    {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        say2func = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSay(AsyncPlayerChatEvent e)
    {
        if (!say2func.containsKey(e.getPlayer().getUniqueId()))
            return;

        FunctionalEntry entry = this.say2func.get(e.getPlayer().getUniqueId());

        if (entry.keywords != null && Arrays.stream(entry.keywords).noneMatch(s -> entry.matchType.apply(e.getMessage(), s)))
            return;
        e.setCancelled(true);

        say2func.remove(e.getPlayer().getUniqueId());
        if (entry.keywords == null)
        {
            entry.func.accept(e.getMessage());
            return;
        }

        entry.func.accept(Arrays.stream(entry.keywords).
                filter(s -> entry.matchType.apply(e.getMessage(), s))
                .collect(Collectors.toList()).get(0));

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onConsole(ServerCommandEvent e)
    {
        if (consoleFunc == null || !(e.getSender() instanceof ConsoleCommandSender))
            return;

        e.setCancelled(true);

        FunctionalEntry entry = consoleFunc;
        if (entry.keywords != null && Arrays.stream(entry.keywords).noneMatch(s -> entry.matchType.apply(e.getCommand(), s)))
            return;
        e.setCancelled(true);

        consoleFunc = null;
        if (entry.keywords == null)
        {
            entry.func.accept(e.getCommand());
            return;
        }

        entry.func.accept(Arrays.stream(entry.keywords)
                .filter(s -> entry.matchType.apply(e.getCommand(), s))
                .collect(Collectors.toList()).get(0));

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        this.say2func.remove(e.getPlayer().getUniqueId());
    }

    public void add(UUID player, FunctionalEntry func)
    {
        if (player == null)
            consoleFunc = func;
        else
            this.say2func.put(player, func);
    }

    public void remove(UUID player)
    {
        if (player == null)
            consoleFunc = null;
        else
            this.say2func.remove(player);
    }

    public FunctionalEntry get(UUID player)
    {
        if (player == null)
            return consoleFunc;
        return this.say2func.get(player);
    }

    public static class FunctionalEntry
    {
        public final String[] keywords;
        public final Consumer<String> func;
        public final BiFunction<String, String, Boolean> matchType;

        public FunctionalEntry(BiFunction<String, String, Boolean> matchType, Consumer<String> runas, String... keywords)
        {
            this(null, matchType, runas, keywords);
        }

        public FunctionalEntry(String comment, BiFunction<String, String, Boolean> matchType, Consumer<String> runas, String... keywords)
        {
            if (keywords.length == 0)
                this.keywords = null;
            else
                this.keywords = keywords;
            this.func = runas;
            this.matchType = matchType;
        }
    }
}
