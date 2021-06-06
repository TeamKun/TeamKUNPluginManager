package net.kunmc.lab.teamkunpluginmanager.console.utils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import net.kunmc.lab.teamkunpluginmanager.common.utils.ClassUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginYamlParser implements Serializable
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
    @SerializedName("default-permission")
    private PrePermission defaultPrePermission;
    public Permission defaultPermission;

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

    public PluginYamlParser parse(HashMap<String, Object> kv)
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

        pluginYamlParser.defaultPermission = flatLine(pluginYamlParser.defaultPrePermission);

        return flatLine(pluginYamlParser);
    }

    private PluginYamlParser flatLine(PluginYamlParser pluginYamlParser)
    {
        PluginYamlParser newParser = SerializationUtils.clone(pluginYamlParser);

        if (pluginYamlParser.permissions == null)
            return pluginYamlParser;

        newParser.permissions = new HashMap<>();

        pluginYamlParser.permissions.forEach((s, permission) -> {
            HashMap<String, Object> children = permission.children;
            if (children == null)
            {
                newParser.permissions.put(s, permission);
                return;
            }

            permission.children = flatLine(children);

            newParser.permissions.put(s, permission);
        });

        return newParser;
    }

    public HashMap<String, Object> flatLine(HashMap<String, Object> children)
    {
        if (children == null)
            return null;

        HashMap<String, Object> newChildren = new HashMap<>(children);

        children.forEach((s1, o) -> {
            if (o instanceof LinkedTreeMap)
            {
                PrePermission perm = new Gson().fromJson(new Gson().toJson(o), PrePermission.class);
                perm.children = flatLine(perm.children);

                newChildren.put(s1, flatLine(perm));
            }
            else
                newChildren.put(s1, o);
        });

        return newChildren;
    }

    public Permission flatLine(PrePermission permission)
    {
        if (permission == null)
            return null;

        Permission perm = new Permission();

        perm.description = permission.description;
        perm.children = permission.children;

        if (defaultPermission == null)
            return perm;

        if (permission.defaultPermission.equalsIgnoreCase("op"))
            perm.defaultPermission = DefaultPermission.OP;
        else if (permission.defaultPermission.equalsIgnoreCase("not op") ||
                permission.defaultPermission.equalsIgnoreCase("not_op"))
            perm.defaultPermission = DefaultPermission.NOTOP;
        else if (permission.defaultPermission.equalsIgnoreCase("true"))
            perm.defaultPermission = DefaultPermission.TRUE;
        else if (permission.defaultPermission.equalsIgnoreCase("false"))
            perm.defaultPermission = DefaultPermission.FALSE;
        return perm;
    }

    public enum Load
    {
        STARTUP,
        POSTWORLD
    }

    public static class Command implements Serializable
    {
        public String description;
        public String[] aliases;
        public String permission;
        @SerializedName("permission-message")
        public String permissionMessage;
        public String usage;
    }

    public static class Permission implements Serializable
    {
        public String description;
        public DefaultPermission defaultPermission;
        public HashMap<String, Object> children;
    }

    public static class PrePermission extends Permission
    {
        @SerializedName("default")
        public String defaultPermission;
    }

    public enum DefaultPermission
    {
        OP,
        NOTOP,
        FALSE,
        TRUE
    }
}
