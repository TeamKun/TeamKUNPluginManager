package net.kunmc.lab.teamkunpluginmanager.console.commands;

import net.kunmc.lab.teamkunpluginmanager.console.PluginManagerConsole;
import net.kunmc.lab.teamkunpluginmanager.console.utils.Installer;
import net.kunmc.lab.teamkunpluginmanager.spigot.TeamKunPluginManager;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.plugin.java.PluginClassLoader;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;

public class CommandInstall implements CommandBase
{

    @Override
    public String getName()
    {
        return "install";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"i"};
    }

    @Override
    public int run(String... args)
    {
        if (args.length == 0)
        {
            printHelp();
            return -1;
        }

        if (containsIgnoreCase(args, "-h") || containsIgnoreCase(args, "--help") || containsIgnoreCase(args, "-?"))
        {
            printHelp();
            return 0;
        }

        Installer.install(args[0], true);

        return 0;
    }

    @Override
    public void printHelp()
    {
        System.out.println("使用法： java -jar " + PluginManagerConsole.classPath + " install <プラグイン>");
        System.out.println();
        System.out.println("パラメータ：");
        System.out.println("    install：インストールするプラグインを指定します。");
        System.out.println("        指定方法：GitHub組織名/リポジトリ名, プラグイン名*, JARへの直接リンク");
        System.out.println();
        System.out.println("エイリアス： i");
        System.out.println("例：");
        System.out.println("    ... install TeamKun/TeamKunPluginManager");
        System.out.println("    ... i https://example.com/plugins/exampleplugin.jar");
        System.out.println("    ... install ExamplePlugin");
        System.out.println();
        System.out.println("*[1]： プラグイン名は事前にリポジトリで定義されている必要があります。");
    }

    public static boolean containsIgnoreCase(String[] target, String maf)
    {
        return Arrays.stream(target).parallel().anyMatch(maf::equalsIgnoreCase);
    }
}
