package org.kunlab.kpm;

import lombok.Getter;
import net.kunmc.lab.peyangpaperutils.PeyangPaperUtils;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandManager;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminals;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.commands.CommandAutoRemove;
import org.kunlab.kpm.commands.CommandClean;
import org.kunlab.kpm.commands.CommandDebug;
import org.kunlab.kpm.commands.CommandInfo;
import org.kunlab.kpm.commands.CommandInstall;
import org.kunlab.kpm.commands.CommandRegister;
import org.kunlab.kpm.commands.CommandResolve;
import org.kunlab.kpm.commands.CommandUninstall;
import org.kunlab.kpm.commands.CommandUpdate;
import org.kunlab.kpm.commands.CommandUpgrade;
import org.kunlab.kpm.commands.CommandUpgradeKPM;
import org.kunlab.kpm.commands.CommandVersion;
import org.kunlab.kpm.installer.impls.uninstall.UninstallArgument;
import org.kunlab.kpm.interfaces.KPMEnvironment;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.lang.LangProvider;
import org.kunlab.kpm.upgrader.KPMUpgrader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

@Getter
public final class TeamKunPluginManager extends JavaPlugin
{
    @Getter
    private static TeamKunPluginManager plugin;
    private FileConfiguration pluginConfig;
    private CommandManager commandManager;

    private KPMRegistry daemon;

    private HeadInstallers headInstallers;
    private KPMUpgrader upgrader;

    @NotNull
    private static String getStringNonNull(FileConfiguration config, String name)
    {
        return Objects.requireNonNull(config.getString(name));
    }

    private static KPMEnvironment buildEnvironment(Plugin plugin, FileConfiguration config)
    {
        Path dataFolder = plugin.getDataFolder().toPath();

        return KPMEnvironmentImpl.builder(plugin)
                .tokenPath(dataFolder.resolve(getStringNonNull(config, "paths.token.body")))
                .tokenKeyPath(dataFolder.resolve(getStringNonNull(config, "paths.token.decryptionKey")))
                .metadataDBPath(dataFolder.resolve(getStringNonNull(config, "paths.database.metadata")))
                .aliasesDBPath(dataFolder.resolve(getStringNonNull(config, "paths.database.aliases")))
                .excludes(config.getStringList("exclude.pluginNames"))
                .organizations(config.getStringList("resolve.githubUsers"))
                .HTTPMaxRedirects(config.getInt("http.maxRedirects"))
                .HTTPUserAgent(getStringNonNull(config, "http.userAgent"))
                .HTTPTimeout(config.getInt("http.timeout"))
                .sources(setupSources(config))
                .build();
    }

    private static HashMap<String, String> setupSources(FileConfiguration config)
    {
        List<Map<?, ?>> aliasSources = config.getMapList("resolve.aliases.sources");

        @SuppressWarnings("unchecked")
        HashMap<String, String> aliasMap = aliasSources.stream()
                .map(map -> (Map<String, ?>) map)
                .map(map -> new Pair<>((String) map.get("name"), (String) map.get("url")))
                .collect(HashMap::new, (map, pair) -> map.put(pair.getLeft(), pair.getRight()), HashMap::putAll);

        return aliasMap;
    }

    private void registerCommands(CommandManager commandManager)
    {
        commandManager.registerCommand("autoremove", new CommandAutoRemove(this));
        commandManager.registerCommand("clean", new CommandClean(this));
        commandManager.registerCommand("info", new CommandInfo(this.daemon));
        commandManager.registerCommand("install", new CommandInstall(this), "add", "i");
        commandManager.registerCommand("register", new CommandRegister(this), "login");
        commandManager.registerCommand("resolve", new CommandResolve(this.daemon));
        commandManager.registerCommand("uninstall", new CommandUninstall(this), "remove", "rm");
        commandManager.registerCommand("update", new CommandUpdate(this, this.daemon));
        commandManager.registerCommand("upgrade", new CommandUpgrade(this));
        commandManager.registerCommand("upgrade-kpm", new CommandUpgradeKPM(this));
        commandManager.registerCommand("version", new CommandVersion(this.daemon), "v", "status");

        if (DebugConstants.DEBUG_MODE)
            commandManager.registerCommand("debug", new CommandDebug(this.daemon));
    }

    @Override
    public void onDisable()
    {
        PeyangPaperUtils.dispose();
        this.daemon.shutdown();
    }

    private void clearCaches()
    {
        Path cacheParent = this.getDataFolder().toPath().resolve(".caches");
        if (!Files.exists(cacheParent))
            try
            {
                Files.createDirectories(cacheParent);
                return;
            }
            catch (IOException e)
            {
                this.getLogger().log(Level.WARNING, "Failed to create caches directory.", e);
            }

        Runner.run(() -> FileUtils.cleanDirectory(cacheParent.toFile()), ((e, bukkitTask) -> this.getLogger().log(Level.WARNING, "Failed to clear caches.", e)));
    }

    private void uninstallUpgraderIfExists()
    {
        Runner.runLater(() -> {
            Plugin updater = Bukkit.getPluginManager().getPlugin("KPMUpgrader");

            if (updater == null)
                return;

            Terminals.ofConsole().info(LangProvider.get("plugin.unused_upgrader_found"));
            this.headInstallers.runUninstall(Terminals.ofConsole(), UninstallArgument.builder(updater)
                    .autoConfirm(true)
                    .build());
        }, 1);  // Run after all plugins are loaded
    }

    private void deleteOldConfig(FileConfiguration config)
    {
        boolean isOld = !config.contains("kpm");

        if (isOld)
        {
            this.getLogger().info(LangProvider.get("plugin.delete_old_config"));
            Path oldConfig = this.getDataFolder().toPath().resolve("config.yml");
            try
            {
                Files.delete(oldConfig);
            }
            catch (IOException e)
            {
                this.getLogger().log(Level.WARNING, LangProvider.get("plugin.delete_old_config.fail"), e);
            }

            this.saveDefaultConfig();
            this.reloadConfig();
        }
    }

    @Override
    public void onEnable()
    {
        plugin = this;

        PeyangPaperUtils.init(this);
        this.saveDefaultConfig();
        this.pluginConfig = this.getConfig();
        this.daemon = new KPMDaemon(buildEnvironment(this, this.pluginConfig));

        try
        {
            LangProvider.init(this.daemon);
            LangProvider.setLanguage(this.pluginConfig.getString("interfaces.lang"));
        }
        catch (IOException e)
        {
            this.getLogger().log(Level.WARNING, "言語ファイルの読み込みに失敗しました(Failed to load language files).", e);
        }
        catch (IllegalArgumentException e)
        {
            this.getLogger().warning("言語ファイルの読み込みに失敗しました(Failed to load a language): " + e.getMessage());
        }


        this.deleteOldConfig(this.getConfig());

        this.commandManager = new CommandManager(this, "kunpluginmanager", "TeamKUNPluginManager", "kpm");
        this.headInstallers = new HeadInstallers(this.daemon);
        this.upgrader = new KPMUpgrader(this, this.daemon);

        this.clearCaches();
        this.registerCommands(this.commandManager);
        this.uninstallUpgraderIfExists();
        Notices.printAllNotice(this.daemon, Terminals.ofConsole());
    }

}
