package net.kunmc.lab.teamkunpluginmanager.console.utils;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PluginYamlParser
{
    public String name;
    public String version;
    public String description;
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

    public PluginYamlParser()
    {
    }

    public PluginYamlParser(File file) throws IOException
    {
        if (!file.exists())
            throw new FileNotFoundException("plugin.yml not found.");

        String d = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        PluginYamlParser pluginYamlParser = new Yaml().loadAs(d, PluginYamlParser.class);

        name = pluginYamlParser.name;
        version = pluginYamlParser.version;
        description = pluginYamlParser.description;
        load = pluginYamlParser.load;
        author = pluginYamlParser.author;
        authors = pluginYamlParser.authors;
        website = pluginYamlParser.website;
        main = pluginYamlParser.main;
        databases = pluginYamlParser.databases;
        prefixes = pluginYamlParser.prefixes;
        depend = pluginYamlParser.depend;
        softdepend = pluginYamlParser.softdepend;
        loadbefore = pluginYamlParser.loadbefore;
        commands = pluginYamlParser.commands;
        permissions = pluginYamlParser.permissions;

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
