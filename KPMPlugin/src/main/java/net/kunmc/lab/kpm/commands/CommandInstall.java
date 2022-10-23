package net.kunmc.lab.kpm.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.TeamKunPluginManager;
import net.kunmc.lab.kpm.installer.impls.install.InstallArgument;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class CommandInstall extends CommandBase
{

    private final TeamKunPluginManager plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        String query = args[0];

        Runner.runAsync(() ->
                this.plugin.getHeadInstallers().runInstall(terminal, new InstallArgument(query))
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
        return "kpm.install";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("クエリからプラグインを新規インストールします。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("query", "string")
        };
    }
}
