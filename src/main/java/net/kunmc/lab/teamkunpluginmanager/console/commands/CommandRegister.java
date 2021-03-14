package net.kunmc.lab.teamkunpluginmanager.console.commands;

import net.kunmc.lab.teamkunpluginmanager.common.Variables;
import net.kunmc.lab.teamkunpluginmanager.console.Progress;
import net.kunmc.lab.teamkunpluginmanager.console.utils.CommandUtil;

public class CommandRegister implements CommandBase
{

    @Override
    public String getName()
    {
        return "register";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"rg"};
    }

    @Override
    public int run(String... args)
    {
        if (args.length < 1)
        {
            printHelp();
            return -1;
        }

        String token = args[0];

        Variables.vault.vault(token);
        System.out.println("S: トークンを正常に保管しました。");
        return 0;
    }

    @Override
    public void printHelp()
    {
        Progress progress = new Progress("ヘルプを読み込み中");
        progress.start();

        String help = CommandUtil.genHelp(
                "register",
                "GitHubの認証に使用するトークンを登録します。",
                getAliases(),
                new CommandUtil.Parameter[]{new CommandUtil.Parameter(
                        "トークン",
                        "登録するトークンです。",
                        "OAuth token, Personal Access Token等",
                        true
                )},
                new String[]{
                        "Your token here!"
                }
        );
        progress.stop();

        System.out.println(help);

    }
}
