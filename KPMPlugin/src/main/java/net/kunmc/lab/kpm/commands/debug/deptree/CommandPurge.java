package net.kunmc.lab.kpm.commands.debug.deptree;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.meta.PluginMetaProvider;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CommandPurge extends CommandBase
{
    private final PluginMetaProvider provider;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        String pluginName = args[0];

        if (pluginName.equalsIgnoreCase("all"))
        {
            Arrays.stream(Bukkit.getPluginManager().getPlugins())
                    .forEach(plugin -> {
                        this.provider.deleteFromDependencyTree(pluginName);
                        this.provider.removePluginMeta(pluginName);

                        terminal.success("Purged " + pluginName);
                    });
        }

        if (!this.provider.isPluginMetaExists(pluginName))
        {
            terminal.error("Cannot find plugin meta of " + pluginName);
            return;
        }

        this.provider.deleteFromDependencyTree(pluginName);
        this.provider.removePluginMeta(pluginName);

        terminal.success("Purged " + pluginName);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                .map(Plugin::getName)
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.debug.deptree.purge";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("プラグインの情報を開放します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("plugin", "Plugin")
        };
    }
}
