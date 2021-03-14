package net.kunmc.lab.teamkunpluginmanager.console.commands;

import net.kunmc.lab.teamkunpluginmanager.console.Progress;
import net.kunmc.lab.teamkunpluginmanager.console.utils.CommandUtil;
import net.kunmc.lab.teamkunpluginmanager.console.utils.Installer;

public class CommandUninstall implements CommandBase
{

    @Override
    public String getName()
    {
        return "uninstall";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"rm", "del", "remove"};
    }

    @Override
    public int run(String... args)
    {
        boolean confirm = CommandUtil.containsIgnoreCase(args, "-y");

        if (CommandUtil.containsIgnoreCase(args, "-f") || CommandUtil.containsIgnoreCase(args, "--force"))
        {
            if (args.length > 1)
                Installer.uninstall(args[0], true, true, confirm);
            return 0;
        }

        if (args.length >= 1)
            Installer.uninstall(args[0], true, false, confirm);
        else
        {
            printHelp();
            return -1;
        }

        return 0;
    }

    @Override
    public void printHelp()
    {
        Progress progress = new Progress("ヘルプを読み込み中");
        progress.start();

        String help = CommandUtil.genHelp(
                "uninstall",
                "プラグインを削除します。",
                getAliases(),
                new CommandUtil.Parameter[]{new CommandUtil.Parameter(
                        "プラグイン",
                        "削除するプラグインです。",
                        "プラグイン名",
                        true
                )},
                new String[]{
                        "ExamplePlugin"
                }
        );
        progress.stop();
        try
        {
            Thread.sleep(10);
        }
        catch (InterruptedException ignored)
        {
        }
        System.out.println(help);

    }
}
