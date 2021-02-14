package net.kunmc.lab.teamkunpluginmanager.console.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
            ArrayList<Command> commands = new ArrayList<>();
            ((HashMap<String, Object>) kv.get("commands")).forEach((s, o) -> {
                commands.add(Command.decode((HashMap<String, Object>) o));
            });
            this.commands = commands.toArray(new Command[0]);
        }

        if (kv.get("permissions") != null)
        {
            ArrayList<Permission> permissions = new ArrayList<>();
            ((HashMap<String, Object>) kv.get("commands")).forEach((s, o) -> {
                permissions.add(Permission.decode((HashMap<String, Object>) o));
            });
            this.permissions = permissions.toArray(new Permission[0]);
        }
    }

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
                return new PluginYamlParser(pluginYamlParser);
            }
        }
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

        @SuppressWarnings("unchecked")
        public static Command decode(HashMap<String, Object> so)
        {
            Command command = new Command();
            command.description = (String) so.get("description");
            ArrayList<String> lst;
            command.aliases = (lst = (ArrayList<String>) so.get("authors")) != null ? lst.toArray(new String[0]): new String[0];
            return command;
        }
    }

    public static class Permission
    {
        public String description;

        public static Permission decode(HashMap<String, Object> so)
        {
            Permission permission = new Permission();
            permission.description = (String) so.get("description");
            return permission;
        }
    }


}
