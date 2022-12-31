package net.kunmc.lab.plugin.kpmupgrader.migrator.migrators;

import net.kunmc.lab.kpm.KPMRegistry;
import net.kunmc.lab.plugin.kpmupgrader.KPMUpgraderPlugin;
import net.kunmc.lab.plugin.kpmupgrader.migrator.KPMMigrateAction;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigMigrator implements KPMMigrateAction
{
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$(?<name>[\\w.]+)\\$");

    private static String replaceVariables(String line, FileConfiguration oldConfig)
    {
        Matcher matcher = VARIABLE_PATTERN.matcher(line);

        while (matcher.find())
        {
            String name = matcher.group("name");
            if (!oldConfig.contains(name))
            {
                System.out.println("Variable " + name + " is not found.");
                continue;
            }

            line = replaceVariable(line, name, oldConfig.get(name));
        }

        return line;
    }

    private static String replaceVariable(String line, String name, Object value)
    {
        int indentSize = getIndentSize(line);
        String indent = StringUtils.repeat("  ", indentSize) + "  ";
        String yamlValue = new Yaml().dump(value);

        String[] lines = yamlValue.split("\r?\n");

        if (lines.length == 1)
            return line.replace("$" + name + "$", yamlValue);

        // object or something

        StringBuilder sb = new StringBuilder("\n");
        for (String l : lines)
            sb.append(indent).append(l).append("\n");

        return line.replace("$" + name + "$", sb.toString());
    }

    private static int getIndentSize(String line)
    {
        int indentSize = 0;

        for (char c : line.toCharArray())
            if (c == ' ' || c == '\t')
                indentSize++;
            else
                break;

        return indentSize;
    }

    @Override
    public void migrate(@NotNull KPMRegistry daemon, @NotNull Path kpmDataFolder)
    {
        Path configPath = kpmDataFolder.resolve("config.yml");
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(configPath.toFile());

        boolean isOld = !oldConfig.contains("kpm");
        if (!isOld)
            return;

        try
        {
            Files.delete(configPath);
            Files.createFile(configPath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            daemon.getLogger().warning("Failed to migrate config.yml");
        }


        ClassLoader classLoader = KPMUpgraderPlugin.class.getClassLoader();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(classLoader.getResourceAsStream("config.v3.yml"))
                ));
             BufferedWriter w = Files.newBufferedWriter(configPath)
        )
        {
            String line;
            while ((line = r.readLine()) != null)
            {
                w.write(replaceVariables(line, oldConfig));
                w.newLine();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            daemon.getLogger().warning("Failed to migrate config.yml");
        }
    }

    @Override
    public String getNeedMigrateVersionRange()
    {
        return "v2.8.3...";
    }
}
