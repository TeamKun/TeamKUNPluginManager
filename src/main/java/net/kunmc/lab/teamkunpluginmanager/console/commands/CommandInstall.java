package net.kunmc.lab.teamkunpluginmanager.console.commands;

import net.kunmc.lab.teamkunpluginmanager.console.Progress;
import net.kunmc.lab.teamkunpluginmanager.console.utils.CommandUtil;
import net.kunmc.lab.teamkunpluginmanager.console.utils.Installer;

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

        if (CommandUtil.containsIgnoreCase(args, "-h") || CommandUtil.containsIgnoreCase(args, "--help") || CommandUtil.containsIgnoreCase(args, "-?"))
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
        Progress progress = new Progress("ヘルプを読み込み中");
        progress.start();

        String help = CommandUtil.genHelp("install",
                "プラグインを新規にインストールします。",
                getAliases(),
                new CommandUtil.Parameter[]{new CommandUtil.Parameter(
                        "プラグイン",
                        "インストールプラグインです。",
                        "GitHub組織名/リポジトリ名, プラグイン名*, JARへの直接リンク",
                        true
                        )},
                new String[]{
                        "TeamKun/ExamplePlugin",
                        "https://example.com/plugins/exampleplugin.jar",
                        "https://github.com/TeamKun/ExamplePlugin/releases/1.0/download/Exampleplugin-1.0.jar",
                        "https://github.com/TeamKun/ExamplePlugin/"
                }
        );
        progress.stop();

        System.out.println(help);

    }


}
