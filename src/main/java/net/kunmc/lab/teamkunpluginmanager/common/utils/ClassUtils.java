package net.kunmc.lab.teamkunpluginmanager.common.utils;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public static Object init(Class<?> type)
    {
        if (type == String.class)
            return "";
        else if (type == Number.class)
            return 0;
        else if (type == List.class || type == ArrayList.class)
            return new ArrayList<>();
        else if (type == Boolean.class)
            return false;
        else if (type == new TypeToken<HashMap<?, ?>>()
        {
        }.getRawType())
            return new HashMap<>();
        else if (type == String[].class)
            return new String[]{};
        return null;
    }
}
