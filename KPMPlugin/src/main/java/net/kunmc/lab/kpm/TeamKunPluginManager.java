package net.kunmc.lab.kpm;

import lombok.Getter;
import net.kunmc.lab.kpm.commands.CommandAutoRemove;
import net.kunmc.lab.kpm.commands.CommandClean;
import net.kunmc.lab.kpm.commands.CommandDebug;
import net.kunmc.lab.kpm.commands.CommandInfo;
import net.kunmc.lab.kpm.commands.CommandInstall;
import net.kunmc.lab.kpm.commands.CommandRegister;
import net.kunmc.lab.kpm.commands.CommandReload;
import net.kunmc.lab.kpm.commands.CommandResolve;
import net.kunmc.lab.kpm.commands.CommandUninstall;
import net.kunmc.lab.kpm.commands.CommandUpdate;
import net.kunmc.lab.kpm.commands.CommandUpgrade;
import net.kunmc.lab.kpm.commands.CommandVersion;
import net.kunmc.lab.peyangpaperutils.PeyangPaperUtils;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public final class TeamKunPluginManager extends JavaPlugin
{
    @Getter
    private static TeamKunPluginManager plugin;
    private FileConfiguration pluginConfig;
    private CommandManager commandManager;

    private KPMDaemon daemon;

    private HeadInstallers headInstallers;

    public void registerCommands(CommandManager commandManager)
    {
        commandManager.registerCommand("autoremove", new CommandAutoRemove(this));
        commandManager.registerCommand("clean", new CommandClean(this));
        commandManager.registerCommand("debug", new CommandDebug());
        commandManager.registerCommand("info", new CommandInfo(this.daemon));
        commandManager.registerCommand("install", new CommandInstall(this), "add", "i");
        commandManager.registerCommand("register", new CommandRegister(this, this.daemon), "login");
        commandManager.registerCommand("reload", new CommandReload(this.daemon));
        commandManager.registerCommand("resolve", new CommandResolve(this.daemon));
        commandManager.registerCommand("uninstall", new CommandUninstall(this), "remove", "rm");
        commandManager.registerCommand("update", new CommandUpdate(this, this.daemon));
        commandManager.registerCommand("upgrade", new CommandUpgrade(this));
        commandManager.registerCommand("version", new CommandVersion(this.daemon), "v");
    }

    @Override
    public void onDisable()
    {
        PeyangPaperUtils.dispose();
        this.daemon.shutdown();
    }

    private HashMap<String, String> setupSources()
    {
        List<Map<?, ?>> aliasSources =
                TeamKunPluginManager.getPlugin().getPluginConfig().getMapList("config");

        @SuppressWarnings("unchecked")
        HashMap<String, String> aliasMap = aliasSources.stream()
                .map(map -> (Map<String, ?>) map)
                .map(map -> new Pair<>((String) map.get("name"), (String) map.get("url")))
                .collect(HashMap::new, (map, pair) -> map.put(pair.getLeft(), pair.getRight()), HashMap::putAll);

        return aliasMap;
    }

    @Override
    public void onEnable()
    {
        PeyangPaperUtils.init(this);
        this.saveDefaultConfig();
        plugin = this;
        this.pluginConfig = this.getConfig();
        this.commandManager = new CommandManager(this, "kunpluginmanager", "TeamKUNPluginManager", "kpm");


        Path dataDir = this.getDataFolder().toPath();

        this.daemon = new KPMDaemon(
                KPMEnvironment.builder(plugin)
                        .tokenPath(dataDir.resolve("token.dat"))
                        .tokenKeyPath(dataDir.resolve("token_key.dat"))
                        .metadataDBPath(dataDir.resolve("plugins.db"))
                        .aliasesDBPath(dataDir.resolve("aliases.db"))
                        .organizations(this.getPluginConfig().getStringList("gitHubName"))
                        .sources(this.setupSources())
                        .build()
        );

        this.headInstallers = new HeadInstallers(this.daemon);

        this.registerCommands(this.commandManager);
    }

}
