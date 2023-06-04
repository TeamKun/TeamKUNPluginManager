package org.kunlab.kpm.nms.v1_16.utils;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.kunlab.kpm.utils.interfaces.ServerConditionChecker;

public class ServerConditionCheckerImpl implements ServerConditionChecker
{
    private final CraftServer craftServer;
    private final int currentSessionReloadCount;

    public ServerConditionCheckerImpl()
    {
        this.craftServer = (CraftServer) Bukkit.getServer();
        this.currentSessionReloadCount = this.craftServer.reloadCount;
    }

    @Override
    public boolean isStopping()
    {
        return this.craftServer.getServer().isRunning();
    }

    @Override
    public boolean isReloading()
    {
        return this.craftServer.reloadCount != this.currentSessionReloadCount;
    }
}
