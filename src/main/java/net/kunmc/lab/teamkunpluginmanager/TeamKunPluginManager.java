package net.kunmc.lab.teamkunpluginmanager;

import net.kunmc.lab.teamkunpluginmanager.commands.CommandMain;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeamKunPluginManager extends JavaPlugin
{
    public static TeamKunPluginManager plugin;
    @Override
    public void onEnable()
    {
        plugin = this;
        Bukkit.getPluginCommand("kunpluginmanager").setExecutor(new CommandMain());
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
