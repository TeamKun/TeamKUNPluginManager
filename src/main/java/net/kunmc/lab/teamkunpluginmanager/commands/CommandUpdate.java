package net.kunmc.lab.teamkunpluginmanager.commands;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.update.UpdateArgument;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandUpdate extends CommandBase
{
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        // TODO: Migrate to new config system
        List<Map<?, ?>> aliasSources =
                TeamKunPluginManager.getPlugin().getPluginConfig().getMapList("config");

        @SuppressWarnings("unchecked")
        HashMap<String, String> aliasMap = aliasSources.stream()
                .map(map -> (Map<String, ?>) map)
                .map(map -> new Pair<>((String) map.get("name"), (String) map.get("url")))
                .collect(HashMap::new, (map, pair) -> map.put(pair.getLeft(), pair.getRight()), HashMap::putAll);

        Runner.runAsync(() ->
                TeamKunPluginManager.getPlugin().getInstallManager().runUpdate(terminal, new UpdateArgument(
                        aliasMap
                ))
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
        return "kpm.update";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("エイリアスをアップデートします。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
