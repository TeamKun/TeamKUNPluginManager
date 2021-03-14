package net.kunmc.lab.teamkunpluginmanager.console.utils;

import com.google.gson.Gson;
import net.kunmc.lab.teamkunpluginmanager.common.utils.ClassUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginYamlParser
{
    public String name;
    public String version;
    public String description;
    public String api_version;
    public Load load;
    public String author;
    public String[] authors;
    public String website;
    public String main;
    public boolean databases;
    public String prefixes;
    public String[] depend;
    public String[] softdepend;
    public String[] loadbefore;
    public HashMap<String, Command> commands;
    public HashMap<String, Permission> permissions;

    public static PluginYamlParser fromJar(File file) throws IOException
    {
        if (!file.exists())
            throw new FileNotFoundException("plugin not found.");


        try (ZipFile zip = new ZipFile(file))
        {

            ZipEntry ent = zip.getEntry("plugin.yml");

            if (ent == null)
                throw new FileNotFoundException("plugin.yml not found.");

            try (InputStream stream = zip.getInputStream(ent))
            {
                HashMap<String, Object> pluginYamlParser = new Yaml().load(stream);
                return new PluginYamlParser().parse(pluginYamlParser);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T castAs(Object main)
    {
        return (T) main;
    }

    public PluginYamlParser parse(HashMap<String, Object> kv) throws IOException
    {

        PluginYamlParser pluginYamlParser = new Gson().fromJson(new Gson().toJson(kv), PluginYamlParser.class);
        for (Field field : pluginYamlParser.getClass().getDeclaredFields())
        {
            field.setAccessible(true);
            try
            {
                if (field.get(pluginYamlParser) == null)
                    field.set(pluginYamlParser, ClassUtils.init(field.getType()));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return pluginYamlParser;
    }

    enum Load
    {
        STARTUP,
        POSTWORLD
    }

    public static class Command
    {
        public String description;
        public String[] aliases;
    }

    public static class Permission
    {
        public String description;

    }


}
