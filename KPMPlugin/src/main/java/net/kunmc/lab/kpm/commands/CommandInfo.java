package net.kunmc.lab.kpm.commands;

import net.kunmc.lab.kpm.KPMDaemon;
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

public class CommandInfo extends CommandBase
{
    private final KPMDaemon daemon;

    public CommandInfo(KPMDaemon daemon)
    {
        this.daemon = daemon;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        terminal.info("プラグイン情報を読み込み中…");
        Plugin plugin = Bukkit.getPluginManager().getPlugin(args[0]);

        if (plugin == null)
        {
            terminal.error("指定されたプラグインは存在しません。");
            return;
        }

        PluginInfoWriter infoWriter = new PluginInfoWriter(this.daemon, terminal, plugin);
        terminal.info("結果を出力しています…");
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
        return of("プラグインの情報を表示します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("pluginName", "string")
        };
    }
}
