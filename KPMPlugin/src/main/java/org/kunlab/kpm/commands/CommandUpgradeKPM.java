package org.kunlab.kpm.commands;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.TeamKunPluginManager;
import org.kunlab.kpm.installer.impls.uninstall.UninstallArgument;
import org.kunlab.kpm.signal.HeadSignalHandlers;
import org.kunlab.kpm.signal.SignalHandleManager;

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
                terminal.success(LangProvider.get("command.upgrade_kpm.no_kpm_upgrader"));
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
        HeadSignalHandlers.getKPMUpgraderHandlers(this.plugin.getDaemon(), terminal)
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
        return (TextComponent) LangProvider.getComponent("command.upgrade_kpm");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
