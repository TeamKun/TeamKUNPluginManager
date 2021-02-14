package net.kunmc.lab.teamkunpluginmanager.console.commands;

import net.kunmc.lab.teamkunpluginmanager.common.DependencyTree;
import net.kunmc.lab.teamkunpluginmanager.console.Progress;
import net.kunmc.lab.teamkunpluginmanager.console.utils.Color;
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
        
        if (CommandUtil.containsIgnoreCase(args, "-h") || CommandUtil.containsIgnoreCase(args, "--help") || CommandUtil.containsIgnoreCase(args, "-?"))
        {
            printHelp();
            return 0;
        }
        switch (args.length)
        {
            case 1:
                uninstall(args[0]);
                break;
            case 2:
                
                uninstall(args[0], Boolean.parseBoolean(args[1]));
                break;
            default:
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
        System.out.println(Color.MAGENTA.format("依存関係ツリーを読み込み中..."));

        DependencyTree.Info info = DependencyTree.getInfo(pluginName, false);
        if (info == null)
        {
            System.out.println(Color.RED.format("E: プラグインが見つかりませんでした。"));
        }
    }

}
