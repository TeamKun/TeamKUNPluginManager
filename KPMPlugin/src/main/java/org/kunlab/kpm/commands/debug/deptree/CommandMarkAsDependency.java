package org.kunlab.kpm.commands.debug.deptree;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.interfaces.meta.PluginMetaProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CommandMarkAsDependency extends CommandBase
{
    private final PluginMetaProvider provider;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        String pluginName = args[0];
        if (!this.provider.isPluginMetaExists(pluginName))
        {
            terminal.error("Cannot find plugin meta of " + pluginName);
            return;
        }


        if (args.length < 2)
        {
            boolean isDependency = this.provider.getPluginMeta(pluginName, false, false).isDependency();
            terminal.success("Plugin " + pluginName + " is " + (isDependency ? "dependency": "not dependency"));

            return;
        }

        boolean isDependency = Boolean.parseBoolean(args[1]);

        this.provider.setDependencyFlag(pluginName, isDependency);

        terminal.success("Marked " + pluginName + " as " + (isDependency ? "dependency": "not dependency"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (args.length == 1)
            return Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                    .map(Plugin::getName)
                    .collect(Collectors.toList());
        else if (args.length == 2)
            return Arrays.asList("true", "false");

        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.debug.deptree.markAsDependency";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("プラグインを依存関係としてマークします。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("pluginName", "Plugin"),
                optional("isDependency", "boolean"),
        };
    }
}
