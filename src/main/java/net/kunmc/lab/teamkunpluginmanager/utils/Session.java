package net.kunmc.lab.teamkunpluginmanager.utils;

public class Session
{
    private volatile boolean session = false;

    public boolean lock()
    {
        if (session)
            return false;
        return session = true;
    }

    public void unlock()
    {
        session = false;
    }

}
