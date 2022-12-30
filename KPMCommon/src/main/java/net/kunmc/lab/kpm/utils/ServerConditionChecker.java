package net.kunmc.lab.kpm.utils;

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
            throw new RuntimeException(e);
        }
    }

    private final long currentSessionReloadCount;

    public ServerConditionChecker()
    {
        try
        {
            this.currentSessionReloadCount = fReloadCount.getLong(Bukkit.getServer());
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean isStopping()
    {
        try
        {
            return !fIsRunning.getBoolean(oMinecraftServer);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isReloading()
    {
        try
        {
            return fReloadCount.getLong(Bukkit.getServer()) != this.currentSessionReloadCount;
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }
}
