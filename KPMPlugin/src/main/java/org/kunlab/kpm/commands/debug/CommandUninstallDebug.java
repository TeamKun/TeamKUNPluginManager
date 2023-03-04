package org.kunlab.kpm.commands.debug;

import lombok.AllArgsConstructor;
import net.kunmc.lab.peyangpaperutils.lib.command.CommandBase;
import net.kunmc.lab.peyangpaperutils.lib.terminal.Terminal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.installer.InstallFailedInstallResult;
import org.kunlab.kpm.installer.impls.uninstall.PluginUninstaller;
import org.kunlab.kpm.installer.impls.uninstall.UnInstallErrorCause;
import org.kunlab.kpm.installer.impls.uninstall.UnInstallTasks;
import org.kunlab.kpm.installer.impls.uninstall.UninstallArgument;
import org.kunlab.kpm.installer.interfaces.InstallResult;
import org.kunlab.kpm.interfaces.KPMRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CommandUninstallDebug extends CommandBase
{
    private final KPMRegistry registry;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        if (indicateArgsLengthInvalid(terminal, args, 1))
            return;

        String query = args[0];

        Runner.runAsync(() -> {
            try
            {
                PluginUninstaller installer =
                        new PluginUninstaller(this.registry, DebugSignalHandler.toManager(terminal));

                InstallResult<UnInstallTasks> installResult = installer.execute(UninstallArgument.builder(query).build());

                if (installResult instanceof InstallFailedInstallResult)
                {
                    InstallFailedInstallResult<UnInstallTasks, UnInstallErrorCause, ?> failedInstallResult =
                            (InstallFailedInstallResult<UnInstallTasks, UnInstallErrorCause, ?>) installResult;

                    terminal.error("Uninstall has failed for " +
                            failedInstallResult.getReason() + " in " + failedInstallResult.getProgress() + " of " +
                            failedInstallResult.getTaskStatus());
                }

                terminal.success("Uninstall succeed: ");
                terminal.info("Installed: " + Arrays.toString(installResult.getInstalled()));
                terminal.info("Pending: " + Arrays.toString(installResult.getPending()));
                terminal.info("Removed: " + Arrays.toString(installResult.getRemoved()));
                terminal.info("Upgraded: " + Arrays.toString(installResult.getUpgraded()));
            }
            catch (Exception e)
            {
                this.registry.getExceptionHandler().report(e);
                terminal.error(e.getClass() + ": " + e.getMessage());
            }
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Terminal terminal, String[] args)
    {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                .map(Plugin::getName).collect(Collectors.toList());
    }

    @Override
    public @Nullable String getPermission()
    {
        return "kpm.debug.uninstall";
    }

    @Override
    public TextComponent getHelpOneLine()
    {
        return of("アンインストールのデバッグコマンドです。");
    }

    @Override
    public String[] getArguments()
    {
        return new String[]{
                required("pluginName", "PluginName")
        };
    }
}
