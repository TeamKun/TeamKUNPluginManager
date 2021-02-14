package net.kunmc.lab.teamkunpluginmanager.console.commands;

import net.kunmc.lab.teamkunpluginmanager.common.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.console.Progress;
import net.kunmc.lab.teamkunpluginmanager.console.utils.CommandUtil;

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
        
        if (CommandUtil.containsIgnoreCase(args, "-f") || CommandUtil.containsIgnoreCase(args, "--force"))
        {
            if (args.length > 1)
                uninstall(args[0], true);
            return 0;
        }

        if (args.length == 1)
            uninstall(args[0]);
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

        String help = CommandUtil.genHelp("uninstall",
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
        catch (InterruptedException ignored) { }
        System.out.println(help);

    }

    private void uninstall(String pluginName)
    {
        uninstall(pluginName, false);
    }

    private void uninstall(String pluginName, boolean force)
    {

        Progress progress = new Progress("依存関係ツリーを読み込み中");
        progress.start();
        DependencyTree.Info info = DependencyTree.getInfo(pluginName, false);
        progress.stop();

        if (info == null)
        {
            System.out.println("E: プラグインが見つかりませんでした。");
            return;
        }


    }

}
