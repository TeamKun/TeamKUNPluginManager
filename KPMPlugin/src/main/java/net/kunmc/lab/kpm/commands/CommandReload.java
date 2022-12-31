package net.kunmc.lab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.KPMRegistry;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
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
public class CommandReload extends CommandBase
{
    private final KPMRegistry registry;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1, 1))
            return;

        Plugin plugin;

        if ((plugin = Bukkit.getPluginManager().getPlugin(args[0])) == null)
        {
            terminal.error("プラグイン %s は存在しません。", args[0]);
            return;
        }

        Runner.runAsync(() -> {
            terminal.info("プラグイン %s を再読み込み中…", args[0]);
            this.registry.getPluginLoader().reloadPlugin(plugin);
            terminal.success("プラグイン %s を正常に再読み込み中しました。", args[0]);
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (args.length == 1)
            return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                    .map(Plugin::getName)
                    .collect(Collectors.toList());
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.reload";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("インストールされているプラグインを再読み込みします。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("pluginName", "string")
        };
    }
}
