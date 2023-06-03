package org.kunlab.kpm.task.loader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.task.interfaces.CommandsPatcher;

import java.util.List;
import java.util.Map;

public class CommandsPatcherBridge implements CommandsPatcher
{
    private final CommandsPatcher target;

    public CommandsPatcherBridge()
    {
        this.target = createCommandSpatcher(getApiVersion());
    }

    private static CommandsPatcher createCommandSpatcher(String apiVersion)
    {
        switch (apiVersion)
        {
            case "1.19":
                return new org.kunlab.kpm.nms.v1_19_4.task.CommandsPatcherImpl();
            case "1.16":
                return new org.kunlab.kpm.nms.v1_16_5.task.CommandsPatcherImpl();
        }

        throw new UnsupportedOperationException("Unsupported API version: " + apiVersion);
    }

    private static String getApiVersion()
    {
        String version = org.bukkit.Bukkit.getServer().getMinecraftVersion();
        String[] split = version.split("\\.");
        return split[0] + "." + split[1];
    }

    @Override
    public CommandMap getCommandMap()
    {
        return this.target.getCommandMap();
    }

    @Override
    public Map<String, Command> getKnownCommands()
    {
        return this.target.getKnownCommands();
    }

    @Override
    public void wrapCommand(Command command, String alias)
    {
        this.target.wrapCommand(command, alias);
    }

    @Override
    public void syncCommandsCraftBukkit()
    {
        this.target.syncCommandsCraftBukkit();
    }

    @Override
    public void unWrapCommand(String command)
    {
        this.target.unWrapCommand(command);
    }

    @Override
    public void patchCommand(@NotNull Plugin plugin, boolean updatePlayer)
    {
        this.target.patchCommand(plugin, updatePlayer);
    }

    @Override
    public void patchCommand(@NotNull Plugin plugin)
    {
        this.target.patchCommand(plugin);
    }

    @Override
    public void unPatchCommand(@NotNull Plugin plugin, boolean updatePlayer)
    {
        this.target.unPatchCommand(plugin, updatePlayer);
    }

    @Override
    public void unPatchCommand(@NotNull Plugin plugin)
    {
        this.target.unPatchCommand(plugin);
    }

    @Override
    public void registerAll(String fallbackPrefix, List<Command> commands)
    {
        this.target.registerAll(fallbackPrefix, commands);
    }
}
