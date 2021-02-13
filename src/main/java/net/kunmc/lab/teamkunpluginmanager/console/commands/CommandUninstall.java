package net.kunmc.lab.teamkunpluginmanager.console.commands;

public class CommandUninstall implements CommandBase {

    @Override
    public String getName() {
        return "uninstall";
    }

    @Override
    public String[] getAliases() {
        return new String[] { "" };
    }

    @Override
    public int run(String... args) {
        return 0;
    }

    @Override
    public void printHelp() {

    }

}
