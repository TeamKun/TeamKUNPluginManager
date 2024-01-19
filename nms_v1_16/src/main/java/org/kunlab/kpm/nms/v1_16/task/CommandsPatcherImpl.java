package org.kunlab.kpm.nms.v1_16.task;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.command.BukkitCommandWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.task.interfaces.CommandsPatcher;
import org.kunlab.kpm.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Bukkitのコマンドをパッチするクラスです。
 */
public class CommandsPatcherImpl implements CommandsPatcher {
    private final CommandDispatcher<CommandListenerWrapper> commandDispatcher;
    private final Field fCommandMap; // Lorg/bukkit/command;SimpleCommandMap
    // -> Ljava/util/Map<Ljava/lang/String;Lorg/bukkit/command/Map>; commandMap;
    private final Field fOwningPlugin; // Lorg/bukkit/command;PluginCommand
    // -> Lorg/bukkit/plugin/Plugin; owningPlugin

    public CommandsPatcherImpl() {
        // INIT NMS

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        this.commandDispatcher = server.getCommandDispatcher().a();

        // Get command related fields
        try {
            Field fCommandMap;
            try
            {
                fCommandMap = ReflectionUtils.getAccessibleField(SimpleCommandMap.class, true, "commandMap");
            }
            catch (NoSuchFieldException e)
            {
                fCommandMap = ReflectionUtils.getAccessibleField(SimpleCommandMap.class, true, "knownCommands");
            }
            this.fCommandMap = fCommandMap;
            this.fOwningPlugin = ReflectionUtils.getAccessibleField(PluginCommand.class, true, "owningPlugin");
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(
                    "Failed to initialize BukkitCommandWrapper: " +
                            "Failed to get command related field(s), " +
                            "Please check your server version and flavor.", e);
        }
    }

    @Override
    public CommandMap getCommandMap() {
        return ((CraftServer) Bukkit.getServer()).getCommandMap();
    }

    @Override
    public Map<String, Command> getKnownCommands() {
        try {
            //noinspection unchecked
            return (Map<String, Command>) this.fCommandMap.get(this.getCommandMap());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void wrapCommand(Command command, String alias) {
        BukkitCommandWrapper wrapper = new BukkitCommandWrapper((CraftServer) Bukkit.getServer(), command);
        wrapper.register(this.commandDispatcher, alias);
    }

    @Override
    public void syncCommandsCraftBukkit() {
        ((CraftServer) Bukkit.getServer()).syncCommands();
    }

    @Override
    public void unWrapCommand(String command) {
        CommandNode<?> root = this.commandDispatcher.getRoot();
        root.removeCommand(command);
    }

    @Override
    public void patchCommand(@NotNull Plugin plugin, boolean updatePlayer) {
        Map<String, Command> commandMap = this.getKnownCommands();

        commandMap.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof PluginIdentifiableCommand)
                .filter(entry -> {
                    PluginIdentifiableCommand command = (PluginIdentifiableCommand) entry.getValue();
                    return command.getPlugin().getName().equalsIgnoreCase(plugin.getName());
                })
                .forEach(entry -> this.wrapCommand(entry.getValue(), entry.getKey()));

        this.syncCommandsCraftBukkit();

        if (updatePlayer)
            Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    @Override
    public void patchCommand(@NotNull Plugin plugin) {
        this.patchCommand(plugin, true);
    }

    @Override
    public void unPatchCommand(@NotNull Plugin plugin, boolean updatePlayer) {
        Map<String, Command> commandMap = this.getKnownCommands();

        // PluginIdentifiable なコマンドを登録解除。
        commandMap.entrySet().stream()
                .filter(nameCommandEntry -> nameCommandEntry.getValue() instanceof PluginIdentifiableCommand)
                .filter(nameCommandEntry -> ((PluginIdentifiableCommand) nameCommandEntry.getValue())
                        .getPlugin().getName().equalsIgnoreCase(plugin.getName()))
                .map(Map.Entry::getKey)
                .forEach(this::unWrapCommand);

        // PluginCommand をコマンド登録解除。
        commandMap.values().stream()
                .filter(command -> command instanceof PluginCommand)
                .filter(command -> this.hasCommand(command, plugin))
                .map(Command::getName)
                .forEach(this::unWrapCommand);

        // 上記２つは似ているようで関係性はないので、こうするしかない。

        this.syncCommandsCraftBukkit();

        if (updatePlayer)
            Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    private boolean hasCommand(Command command, Plugin plugin) {
        if (!(command instanceof PluginCommand))
            return false;

        try {
            Plugin owningPlugin = (Plugin) this.fOwningPlugin.get(command);
            return plugin == owningPlugin;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void unPatchCommand(@NotNull Plugin plugin) {
        this.unPatchCommand(plugin, true);
    }

    @Override
    public void registerAll(String fallbackPrefix, List<Command> commands) {
        this.getCommandMap().registerAll(fallbackPrefix, commands);
    }
}
