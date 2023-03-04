package org.kunlab.kpm.utils;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public class ServerConditionChecker
{
    private static final Field fReloadCount;  // Lorg/craftbukkit/<version>/CraftServer; -> reloadCount:I
    private static final Field fIsRunning;  // Lnet/minecraft/server/<version>/MinecraftServer; -> isRunning:Z
    private static final Object oMinecraftServer;  // Lnet/minecraft/server/<version>/MinecraftServer; -> Lnet/minecraft/server/<version>/DedicatedServer;


    static
    {
        try
        {
            Class<?> cCraftServer = ReflectionUtils.PackageType.CRAFTBUKKIT.getClass("CraftServer");
            fReloadCount = ReflectionUtils.getAccessibleField(cCraftServer, true, "reloadCount");
            Class<?> cMinecraftServer = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("MinecraftServer");
            fIsRunning = ReflectionUtils.getAccessibleField(cMinecraftServer, true, "isRunning");
            oMinecraftServer = ReflectionUtils.getValue(Bukkit.getServer(), true, "console");
        }
        catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private final long currentSessionReloadCount;

    @SneakyThrows(IllegalAccessException.class)
    public ServerConditionChecker()
    {
        this.currentSessionReloadCount = fReloadCount.getLong(Bukkit.getServer());
    }

    public boolean isStopping()
    {
        try
        {
            return !fIsRunning.getBoolean(oMinecraftServer);
        }
        catch (IllegalAccessException e)
        {
            return false;
        }
    }

    @SneakyThrows(IllegalAccessException.class)
    public boolean isReloading()
    {
        return fReloadCount.getLong(Bukkit.getServer()) != this.currentSessionReloadCount;
    }
}
