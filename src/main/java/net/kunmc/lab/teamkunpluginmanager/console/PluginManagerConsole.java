package net.kunmc.lab.teamkunpluginmanager.console;

import com.zaxxer.hikari.HikariDataSource;
import develop.p2p.lib.FileConfiguration;
import develop.p2p.lib.Intellij;
import develop.p2p.lib.FieldModifier;
import net.kunmc.lab.teamkunpluginmanager.common.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.common.Variables;
import net.kunmc.lab.teamkunpluginmanager.console.commands.CommandHelp;
import net.kunmc.lab.teamkunpluginmanager.console.commands.CommandInstall;
import net.kunmc.lab.teamkunpluginmanager.console.commands.stracture.CommandBase;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PluginManagerConsole
{
    public static final CommandBase[] commands = {new CommandInstall()};

    public static FileConfiguration config;
    public static Path dataFolder;

    public static void main(String[] args)
    {
        int exitcode = 0;

        init();

        switch (args.length)
        {
            case 0:
                new CommandHelp().run();
                exitcode = 1;
                break;
            case 1:
                List<CommandBase> c = Arrays.stream(commands).parallel().filter(commandBase -> commandBase.getName().equalsIgnoreCase(args[0]) ||
                        containsIgnoreCase(commandBase.getAliases(), args[0])).collect(Collectors.toList());

                String[] realArgs = ArrayUtils.remove(args, 0);

                if (c.size() < 1)
                    new CommandHelp().run(realArgs);
                else
                    c.get(0).run(ArrayUtils.remove(args, 0));
                break;
        }

        System.exit(exitcode);
    }


    public static void init()
    {
        String classPath =  System.getProperty("java.class.path");
        if (Intellij.isDebugging())
            classPath = StringUtils.split(classPath, ";")[0];
        dataFolder = new File(new File(classPath).getParentFile(), "TeamKunPluginManager").toPath();

        dataFolder.toFile().mkdirs();

        config = new FileConfiguration(dataFolder.toFile(), "config.yml");

        config.saveDefaultConfig();

        Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.OFF);
    }

    public static boolean containsIgnoreCase(String[] target, String maf)
    {
        return Arrays.stream(target).parallel().anyMatch(maf::equalsIgnoreCase);
    }

    public static boolean isTokenAvailable()
    {
        return !Variables.OAuthToken.isEmpty();
    }
}
