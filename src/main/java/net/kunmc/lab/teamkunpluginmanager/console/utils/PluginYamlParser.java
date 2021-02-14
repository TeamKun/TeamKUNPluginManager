package net.kunmc.lab.teamkunpluginmanager.console.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.permissions.Permission;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    public Command[] commands;
    public Permission[] permissions;

    public static PluginYamlParser fromJar(File file) throws IOException
    {
        if (!file.exists())
            throw new FileNotFoundException("plugin not found.");

        ZipFile zip = new ZipFile(file);

        ZipEntry ent = zip.getEntry("plugin.yml");

        if (ent == null)
            throw new FileNotFoundException("plugin.yml not found.");

        return PluginYamlParser.fromStream(zip.getInputStream(ent));
    }

    public static PluginYamlParser fromStream(InputStream stream) throws IOException
    {
        String d = IOUtils.toString(stream, StandardCharsets.UTF_8);

        HashMap<String, Object> pluginYamlParser = new Yaml().load(d);
        return new PluginYamlParser(pluginYamlParser);
    }

    @SuppressWarnings("unchecked")
    public PluginYamlParser(HashMap<String, Object> kv) throws IOException
    {

        name = (String) kv.get("name");
        version = (String) kv.get("version");
        description = (String) kv.get("description");
        load = (Load) kv.get("load");
        author = (String) kv.get("author");
        website = (String) kv.get("website");
        main = (String) kv.get("main");
        databases = Boolean.parseBoolean((String) kv.get("databases"));
        prefixes = (String) kv.get("prefixes");

        ArrayList<String> lst;
        authors = (lst = (ArrayList<String>) kv.get("authors")) != null ? lst.toArray(new String[0]): new String[0];
        depend = (lst = (ArrayList<String>) kv.get("depend")) != null ? lst.toArray(new String[0]): new String[0];
        softdepend = (lst = (ArrayList<String>) kv.get("softdepend")) != null ? lst.toArray(new String[0]): new String[0];
        loadbefore = (lst = (ArrayList<String>) kv.get("loadbefore")) != null ? lst.toArray(new String[0]): new String[0];


        if (kv.get("commands") != null)
        {
            ArrayList<Command> commands = new ArrayList<>();;
            ((HashMap<String, Object>) kv.get("commands")).forEach((s, o) -> {
                commands.add(Command.decode((HashMap<String, Object>) o));
            });
            this.commands = commands.toArray(new Command[0]);
        }

        if (kv.get("permissions") != null)
        {
            ArrayList<Permission> permissions = new ArrayList<>();;
            ((HashMap<String, Object>) kv.get("commands")).forEach((s, o) -> {
                permissions.add(Permission.decode((HashMap<String, Object>) o));
            });
            this.permissions = permissions.toArray(new Permission[0]);
        }
    }


    enum Load
    {
        STARTUP,
        POSTWORLD
    }

    public static class Command
    {
        @SuppressWarnings("unchecked")
        public static Command decode(HashMap<String, Object> so)
        {
            Command command = new Command();
            command.description = (String) so.get("description");
            ArrayList<String> lst;
            command.aliases = (lst = (ArrayList<String>) so.get("authors")) != null ? lst.toArray(new String[0]): new String[0];
            return command;
        }
        public String description;
        public String[] aliases;
    }

    public static class Permission
    {
        public static Permission decode(HashMap<String, Object> so)
        {
            Permission permission = new Permission();
            permission.description = (String) so.get("description");
            return permission;
        }
        public String description;
    }


}
