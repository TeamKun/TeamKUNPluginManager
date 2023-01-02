package net.kunmc.lab.kpm.commands;

import net.kunmc.lab.kpm.TeamKunPluginManager;
import net.kunmc.lab.kpm.installer.impls.uninstall.UninstallArgument;
import net.kunmc.lab.kpm.signal.HeadSignalHandlers;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommandUpgradeKPM extends CommandBase
{
    private final TeamKunPluginManager plugin;

    public CommandUpgradeKPM(TeamKunPluginManager plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 0, 1))
            return;

        boolean isDestructMode = args.length == 1 && args[0].equals("destruct");

        if (isDestructMode)
        {
            Plugin kpmUpgrader;
            if ((kpmUpgrader = Bukkit.getPluginManager().getPlugin("KPMUpgrader")) == null)
            {
                terminal.success("KPM アップグレーダはインストールされていません。");
                return;
            }

            Runner.runAsync(() ->
                    this.plugin.getHeadInstallers().runUninstall(
                            terminal,
                            UninstallArgument.builder(kpmUpgrader)
                                    .autoConfirm(true)
                                    .build()
                    )
            );

            return;
        }

        SignalHandleManager signalHandleManager = new SignalHandleManager();
        HeadSignalHandlers.getKPMUpgraderHandlers(terminal)
                .forEach(signalHandleManager::register);

        Runner.runAsync(() ->
                this.plugin.getUpgrader().runUpgrade(signalHandleManager)
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "upgrade-kpm";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("KPM をアップグレードします。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
