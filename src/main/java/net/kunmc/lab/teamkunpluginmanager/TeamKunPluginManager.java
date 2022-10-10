package net.kunmc.lab.teamkunpluginmanager;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandAutoRemove;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandClean;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandDebug;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandInfo;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandInstall;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandRegister;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandReload;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandResolve;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandStatus;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandUninstall;
import net.kunmc.lab.teamkunpluginmanager.commands.CommandUpdate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

@Getter
public final class TeamKunPluginManager extends JavaPlugin
{
    @Getter
    private static TeamKunPluginManager plugin;
    private FileConfiguration pluginConfig;
    private CommandManager commandManager;

    private KPMDaemon daemon;

    public static void registerCommands(KPMDaemon daemon, CommandManager commandManager)
    {
        commandManager.registerCommand("autoremove", new CommandAutoRemove(daemon));
        commandManager.registerCommand("clean", new CommandClean(daemon));
        commandManager.registerCommand("info", new CommandInfo(daemon));
        commandManager.registerCommand("install", new CommandInstall(daemon), "add", "i");
        commandManager.registerCommand("register", new CommandRegister(daemon), "login");
        commandManager.registerCommand("reload", new CommandReload(daemon));
        commandManager.registerCommand("resolve", new CommandResolve(daemon));
        commandManager.registerCommand("status", new CommandStatus(daemon));
        commandManager.registerCommand("uninstall", new CommandUninstall(daemon), "remove", "rm");
        commandManager.registerCommand("update", new CommandUpdate(daemon));
        commandManager.registerCommand("debug", new CommandDebug());
    }

    @Override
    public void onDisable()
    {
        this.daemon.shutdown();
    }

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        plugin = this;
        this.pluginConfig = getConfig();
        this.commandManager = new CommandManager(this, "kunpluginmanager", "TeamKUNPluginManager", "kpm");


        Path dataDir = this.getDataFolder().toPath();

        this.daemon = new KPMDaemon(
                this,
                this.getSLF4JLogger(),
                dataDir,
                dataDir.resolve("plugins.db"),
                dataDir.resolve("token.dat"),
                dataDir.resolve("token_key.dat"),
                dataDir.resolve("aliases.db")
        );

        this.daemon.setupDaemon(this.getDataFolder().toPath());

        registerCommands(this.daemon, commandManager);
    }

}
