package net.kunmc.lab.teamkunpluginmanager.console;

import develop.p2p.lib.FileConfiguration;
import develop.p2p.lib.Intellij;
import net.kunmc.lab.teamkunpluginmanager.common.Variables;
import net.kunmc.lab.teamkunpluginmanager.console.commands.CommandBase;
import net.kunmc.lab.teamkunpluginmanager.console.commands.CommandHelp;
import net.kunmc.lab.teamkunpluginmanager.console.commands.CommandInstall;
import net.kunmc.lab.teamkunpluginmanager.console.commands.CommandUninstall;
import net.kunmc.lab.teamkunpluginmanager.console.utils.CommandUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PluginManagerConsole
{
    public static final CommandBase[] commands = {new CommandInstall(), new CommandUninstall()};

    public static FileConfiguration config;
    public static Path dataFolder;
    public static String classPath;

    public static void main(String[] args)
    {
        int exitcode = 0;

        Progress progress = new Progress("情報を読み込み中").start();
        init();
        progress.stop();

        if (args.length == 0)
        {
            new CommandHelp().run();
            System.exit(exitcode);
            return;
        }

        String[] finalArgs = args;
        List<CommandBase> c = Arrays.stream(commands).parallel().filter(commandBase -> commandBase.getName().equalsIgnoreCase(finalArgs[0]) ||
                containsIgnoreCase(commandBase.getAliases(), finalArgs[0])).collect(Collectors.toList());

        String[] realArgs = ArrayUtils.remove(args, 0);

        if (c.size() < 1)
            new CommandHelp().run(realArgs);
        else
        {
            args = ArrayUtils.remove(args, 0);
            if (CommandUtil.containsIgnoreCase(args, "-h") || CommandUtil.containsIgnoreCase(args, "--help") || CommandUtil.containsIgnoreCase(args, "-?"))
                c.get(0).printHelp();
            else
                System.exit(c.get(0).run(args));
        }
    }

    public static void init()
    {
        classPath = System.getProperty("java.class.path");
        if (Intellij.isDebugging())
            classPath = StringUtils.split(classPath, ";")[0];
        dataFolder = new File(new File(classPath).getParentFile(), "TeamKunPluginManager").toPath();

        dataFolder.toFile().mkdirs();

        config = new FileConfiguration(dataFolder.toFile(), "config.yml");

        config.saveDefaultConfig();

        Variables.OAuthToken = config.getString("oauth");
        if (config.get("gitHubName") instanceof String)
            Variables.gitHubName = new String[]{config.get("gitHubName")};
        else
            Variables.gitHubName = config.get("gitHubName");

        Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.OFF);

        if (!AnsiConsole.isInstalled())
        {
            AnsiConsole.systemInstall();
            Runtime.getRuntime().addShutdownHook(new Thread(AnsiConsole::systemUninstall));
        }
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
