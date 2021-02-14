package net.kunmc.lab.teamkunpluginmanager.console.commands;

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

        switch (args[0])
        {
            case "-h":
            case "--help":
                printHelp();
                break;
            default:
                Installer.install(args[0], true);
                break;
        }

        return 0;
    }

    @Override
    public void printHelp()
    {

    }

}
