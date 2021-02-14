package net.kunmc.lab.teamkunpluginmanager.common.utils;

public class ClassUtils
{
    public static boolean isExists(String name)
    {
        try
        {
            Class.forName(name);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
