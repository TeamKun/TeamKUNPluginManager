package net.kunmc.lab.teamkunpluginmanager.console.utils;

import net.kunmc.lab.teamkunpluginmanager.console.PluginManagerConsole;

import java.io.IOException;
import java.util.Properties;

public class Property
{
    private static final Properties properties;

    static
    {
        properties = new Properties();
        try
        {
            properties.load(PluginManagerConsole.class.getClassLoader().getResourceAsStream("console.properties"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("プロパティをロードできませんでした。");
            System.exit(1);
        }
    }

    public static Properties getProperty()
    {
        return properties;
    }
}
