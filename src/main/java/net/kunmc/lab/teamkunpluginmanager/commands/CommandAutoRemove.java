package net.kunmc.lab.teamkunpluginmanager.commands;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.KPMDaemon;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.impls.autoremove.AutoRemoveArgument;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class CommandAutoRemove extends CommandBase
{
    private final KPMDaemon daemon;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        Runner.runAsync(() ->
                this.daemon.getInstallManager().runAutoRemove(terminal, new AutoRemoveArgument())
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
        return "kpm.autoremove";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("必要がなくなったプラグインを自動で削除します。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[0];
    }
}
