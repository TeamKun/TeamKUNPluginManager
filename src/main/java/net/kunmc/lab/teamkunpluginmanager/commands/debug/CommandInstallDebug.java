package net.kunmc.lab.teamkunpluginmanager.commands.debug;

import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.install.InstallErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.install.InstallPhases;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.install.PluginInstaller;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CommandInstallDebug extends CommandBase
{
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        String query = args[0];

        Runner.runAsync(() -> {
            try
            {
                PluginInstaller installer = new PluginInstaller(new DebugSignalHandler(terminal));

                InstallResult<InstallPhases> installResult = installer.execute(query);

                if (installResult instanceof InstallFailedInstallResult)
                {
                    InstallFailedInstallResult<InstallPhases, InstallErrorCause, ?> failedInstallResult =
                            (InstallFailedInstallResult<InstallPhases, InstallErrorCause, ?>) installResult;

                    terminal.error("Install has failed for " +
                            failedInstallResult.getReason() + " in " + failedInstallResult.getProgress() + " of " +
                            failedInstallResult.getPhaseStatus());
                }

                terminal.success("Install succeed: ");
                terminal.info("Installed: " + Arrays.toString(installResult.getInstalled()));
                terminal.info("Pending: " + Arrays.toString(installResult.getPending()));
                terminal.info("Removed: " + Arrays.toString(installResult.getRemoved()));
                terminal.info("Upgraded: " + Arrays.toString(installResult.getUpgraded()));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                terminal.error(e.getClass() + ": " + e.getMessage());
            }
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return null;
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.debug.install";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("インストールのデバッグコマンドです。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("query", "Query")
        };
    }
}
