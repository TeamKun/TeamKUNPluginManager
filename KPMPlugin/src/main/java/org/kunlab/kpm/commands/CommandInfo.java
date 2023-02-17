package org.kunlab.kpm.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.lang.LangProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandInfo extends CommandBase
{
    private final KPMRegistry registry;

    public CommandInfo(KPMRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        terminal.info(LangProvider.get("command.info.loading"));
        Plugin plugin = Bukkit.getPluginManager().getPlugin(args[0]);

        if (plugin == null)
        {
            terminal.error(LangProvider.get("general.plugin.notFound"));
            return;
        }

        PluginInfoWriter infoWriter = new PluginInfoWriter(this.registry, terminal, plugin);
        infoWriter.write();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                .map(Plugin::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.info";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return LangProvider.getComponent("command.info");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("pluginName", "string")
        };
    }
}
