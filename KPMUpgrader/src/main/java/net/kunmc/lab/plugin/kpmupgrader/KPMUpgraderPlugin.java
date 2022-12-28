package net.kunmc.lab.plugin.kpmupgrader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class KPMUpgraderPlugin extends JavaPlugin implements CommandExecutor
{
    private final UpgradeImpl impl;

    public KPMUpgraderPlugin()
    {
        this.impl = new UpgradeImpl(this);
    }

    @Override
    public void onEnable()
    {
        Objects.requireNonNull(this.getCommand("kpm-upgrade-internal")).setExecutor(this);
    }

    private boolean commandError(CommandSender sender)
    {
        sender.sendMessage("You cannot use this command.");
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @Nonnull @NotNull String[] args)
    {
        if (!(sender instanceof ConsoleCommandSender))
            return this.commandError(sender);

        if (args.length == 0)
            return this.commandError(sender);

        String toVersion = args[0];

        this.impl.runUpgrade(toVersion);
        return true;
    }
}
