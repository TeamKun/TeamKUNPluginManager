package net.kunmc.lab.teamkunpluginmanager;

import net.kunmc.lab.teamkunpluginmanager.commands.CommandMain;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeamKunPluginManager extends JavaPlugin
{
    public static TeamKunPluginManager plugin;
    public static FileConfiguration config;
    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        plugin = this;
        config = getConfig();
        Bukkit.getPluginCommand("kunpluginmanager").setExecutor(new CommandMain());
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }
}
