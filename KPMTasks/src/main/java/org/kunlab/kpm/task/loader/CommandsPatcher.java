package org.kunlab.kpm.task.loader;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.utils.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Bukkitのコマンドをパッチするクラスです。
 */
public class CommandsPatcher
{
    private CommandDispatcher<?> iBrigadierCommandDispatcher;

    private Constructor<?> coBukkitCommandWrapper; // Lorg/bukkit/craftbukkit/<version>/command/BukkitCommandWrapper;

    private Method mRegisterCommand; // Lorg/bukkit/craftbukkit/<version>/command/BukkitCommandWrapper;
    // ->registerCommand(Lcom/mojang/brigadier/CommandDispatcher;Ljava/lang/String);V
    private Method mSyncCommands; // Lorg/bukkit/craftbukkit/<version>/CraftServer;
    // ->syncCommands();V

    private Field fCommandMap; // Lorg/bukkit/craftbukkit/<version>/CraftServer;
    // ->org/bukkit/command/SimpleCommandMap; commandMap;
    private Field fChildren; // Lcom/mojang/brigadier/tree/CommandNode;
    // ->java/util/Map<Ljava/lang/String;Lcom/mojang/brigadier/tree/CommandNode<>; children;
    private Field fLiterals; // Lcom/mojang/brigadier/tree/CommandNode;
    // ->java/util/Map<Ljava/lang/String;Lcom/mojang/brigadier/tree/LiteralCommandNode<>; literals;
    private Field fArguments; // Lcom/mojang/brigadier/tree/CommandNode;
    // ->java/util/Map<Ljava/lang/String;Lcom/mojang/brigadier/tree/ArgumentCommandNode<>; arguments;

    /**
     * BukkitCommandWrapperのコンストラクタ。
     *
     * @throws IllegalArgumentException 初期化に失敗したりした場合
     */
    public CommandsPatcher()
    {
        checkAvailable();
        this.initReflections();
    }

    private static void checkAvailable()
    {
        try
        {
            Class.forName("com.mojang.brigadier.CommandDispatcher");
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException(generateFailedMessage("Couldn't find the class 'com.mojang.brigadier.CommandDispatcher'"));
        }
    }

    private static String generateFailedMessage(String cause)
    {
        return String.format("Failed to initialize BukkitCommandWrapper: %s, Please check your server version and flavor.", cause);
    }

    private void initReflections()
    {
        // Lnet/minecraft/server/<version>/MinecraftServer;
        Class<?> cMinecraftServer;
        Object iMinecraftServer;

        // Get MinecraftServer instance
        try
        {
            cMinecraftServer = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("MinecraftServer");
            Method mGetServer = ReflectionUtils.getAccessibleMethod(cMinecraftServer, "getServer");
            iMinecraftServer = mGetServer.invoke(null);
        }
        catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            throw new IllegalStateException(generateFailedMessage("Failed to get MinecraftServer instance"), e);
        }

        // Lnet/minecraft/command/CommandDispatcher;
        Object iCommandDispatcher;

        // Get CommandDispatcher instance
        try
        {
            Method fCommandDispatcher = ReflectionUtils.getAccessibleMethod(cMinecraftServer, "getCommandDispatcher");
            iCommandDispatcher = fCommandDispatcher.invoke(iMinecraftServer);
        }
        catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
        {
            throw new IllegalStateException(generateFailedMessage("Failed to get CommandDispatcher instance"), e);
        }

        // Get CommandDispatcher(Brigadier) instance
        try
        {
            Method mA = ReflectionUtils.getAccessibleMethod(iCommandDispatcher.getClass(), "a");
            this.iBrigadierCommandDispatcher = (CommandDispatcher<?>) mA.invoke(iCommandDispatcher);
        }
        catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassCastException e)
        {
            throw new IllegalStateException(generateFailedMessage("Failed to get CommandDispatcher(Brigadier) instance"), e);
        }

        Class<?> cBukkitCommandWrapper;
        Class<?> cCraftServer;

        // Get BukkitCommandWrapper constructor
        try
        {
            cBukkitCommandWrapper = ReflectionUtils.PackageType.CRAFTBUKKIT_COMMAND.getClass("BukkitCommandWrapper");
            cCraftServer = ReflectionUtils.PackageType.CRAFTBUKKIT.getClass("CraftServer");
            this.coBukkitCommandWrapper = ReflectionUtils.getAccessibleConstructor(cBukkitCommandWrapper, cCraftServer, Command.class);
        }
        catch (ClassNotFoundException | NoSuchMethodException e)
        {
            throw new IllegalStateException(generateFailedMessage("Failed to get BukkitCommandWrapper constructor"), e);
        }

        // Get registerCommand method
        try
        {
            this.mRegisterCommand = ReflectionUtils.getAccessibleMethod(cBukkitCommandWrapper, "register",
                    CommandDispatcher.class, String.class
            );
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalStateException(generateFailedMessage("Failed to get registerCommand method"), e);
        }

        // Get syncCommands method
        try
        {
            this.mSyncCommands = ReflectionUtils.getAccessibleMethod(cCraftServer, "syncCommands");
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalStateException(generateFailedMessage("Failed to get syncCommands method"), e);
        }

        // Get commandMap field
        try
        {
            this.fCommandMap = ReflectionUtils.getAccessibleField(cCraftServer, true, "commandMap");
        }
        catch (NoSuchFieldException e)
        {
            throw new IllegalStateException(generateFailedMessage("Failed to get commandMap field"), e);
        }

        // Get brigadier field(s)
        try
        {
            this.fChildren = ReflectionUtils.getAccessibleField(CommandNode.class, true, "children");
            this.fLiterals = ReflectionUtils.getAccessibleField(CommandNode.class, true, "literals");
            this.fArguments = ReflectionUtils.getAccessibleField(CommandNode.class, true, "arguments");
        }
        catch (NoSuchFieldException e)
        {
            throw new IllegalStateException(generateFailedMessage("Failed to get brigadier field(s)."), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void removeCommandFromNode(CommandNode<?> commandNode, String commandName)
    {
        try
        {
            CommandNode<?> child = commandNode.getChild(commandName);
            if (child == null)
                return;

            if (child instanceof LiteralCommandNode)
                ((Map<String, LiteralCommandNode<?>>) this.fLiterals.get(commandNode))
                        .remove(commandName);
            else if (child instanceof ArgumentCommandNode)
                ((Map<String, ArgumentCommandNode<?, ?>>) this.fArguments.get(commandNode))
                        .remove(commandName);

            ((Map<String, CommandNode<?>>) this.fChildren.get(commandNode)).remove(commandName);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * CommandMapを取得します。
     *
     * @return CommandMap
     */
    @SneakyThrows(IllegalAccessException.class)
    public CommandMap getCommandMap()
    {
        return (SimpleCommandMap) this.fCommandMap.get(Bukkit.getServer());
    }

    /**
     * 知られているコマンドを取得します。
     *
     * @return 知られているコマンド
     */
    public Map<String, Command> getKnownCommands()
    {
        return this.getCommandMap().getKnownCommands();
    }

    /**
     * コマンドをラップします。
     *
     * @param command ラップするコマンド
     * @param alias   コマンドのエイリアス
     */
    public void wrapCommand(Command command, String alias)
    {
        try
        {
            Object oBukkitCommandWrapper = this.coBukkitCommandWrapper.newInstance(Bukkit.getServer(), command);
            this.mRegisterCommand.invoke(oBukkitCommandWrapper, this.iBrigadierCommandDispatcher, alias);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            throw new IllegalStateException("Failed to wrap command", e);
        }
    }

    /**
     * CraftServerのsyncCommandsを呼び出す。
     */
    public void syncCommandsCraftBukkit()
    {
        try
        {
            this.mSyncCommands.invoke(Bukkit.getServer());
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new IllegalStateException("Failed to sync commands", e);
        }
    }

    public void unWrapCommand(String command)
    {
        this.removeCommandFromNode(this.iBrigadierCommandDispatcher.getRoot(), command);
    }

    /**
     * コマンドをパッチします。
     *
     * @param plugin       プラグイン
     * @param updatePlayer プレイヤーにコマンドの変更を通知するか({@link Player#updateCommands()})
     */
    public void patchCommand(@NotNull Plugin plugin, boolean updatePlayer)
    {
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

    /**
     * コマンドをパッチします。
     *
     * @param plugin パッチするプラグイン
     */
    public void patchCommand(@NotNull Plugin plugin)
    {
        this.patchCommand(plugin, true);
    }

    /**
     * コマンドをアンパッチします。
     *
     * @param plugin       アンパッチするプラグイン
     * @param updatePlayer プレイヤーにコマンドの変更を通知するか({@link Player#updateCommands()})
     */
    public void unPatchCommand(@NotNull Plugin plugin, boolean updatePlayer)
    {
        Map<String, Command> commandMap = this.getKnownCommands();

        commandMap.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof PluginIdentifiableCommand)
                .filter(entry -> {
                    PluginIdentifiableCommand command = (PluginIdentifiableCommand) entry.getValue();
                    return command.getPlugin().getName().equalsIgnoreCase(plugin.getName());
                })
                .map(Map.Entry::getKey)
                .forEach(this::unWrapCommand);

        commandMap.entrySet().stream()
                .filter(entry -> Plugin.class.isAssignableFrom(entry.getValue().getClass()))
                .filter(entry -> {
                    Field fPlugin = Arrays.stream(entry.getValue().getClass().getDeclaredFields()).parallel()
                            .filter(field -> field.getType().isAssignableFrom(Plugin.class))
                            .findFirst().orElse(null);
                    if (fPlugin == null)
                        return false;

                    fPlugin.setAccessible(true);
                    try
                    {
                        return ((Plugin) fPlugin.get(entry.getValue())).getName().equalsIgnoreCase(plugin.getName());
                    }
                    catch (IllegalAccessException e)
                    {
                        return false;
                    }
                })
                .map(Map.Entry::getKey)
                .forEach(this::unWrapCommand);

        this.syncCommandsCraftBukkit();

        if (updatePlayer)
            Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    /**
     * コマンドをアンパッチします。
     *
     * @param plugin プラグイン
     */
    public void unPatchCommand(@NotNull Plugin plugin)
    {
        this.unPatchCommand(plugin, true);
    }

    /**
     * コマンドをすべて登録します。
     *
     * @param fallbackPrefix フォールバックのプレフィックス
     * @param commands       コマンド
     */
    public void registerAll(String fallbackPrefix, List<Command> commands)
    {
        this.getCommandMap().registerAll(fallbackPrefix, commands);
    }
}