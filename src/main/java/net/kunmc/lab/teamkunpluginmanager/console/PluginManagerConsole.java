package net.kunmc.lab.teamkunpluginmanager.console;

import com.avaje.ebeaninternal.server.el.ElSetValue;
import net.kunmc.lab.teamkunpluginmanager.console.commands.CommandHelp;
import net.kunmc.lab.teamkunpluginmanager.console.commands.CommandInstall;
import net.kunmc.lab.teamkunpluginmanager.console.commands.stracture.CommandBase;
import org.apache.commons.lang.ArrayUtils;
import sun.swing.CachedPainter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PluginManagerConsole
{
    public static final CommandBase[] commands = {new CommandInstall()};

    public static void main(String[] args)
    {
        int exitcode = 0;

        switch (args.length) {
            case 0:
                new CommandHelp().run();
                exitcode = 1;
                break;
            case 1:
                List<CommandBase> c =  Arrays.stream(commands).parallel().filter(commandBase -> commandBase.getName().equalsIgnoreCase(args[0]) ||
                        containsIgnoreCase(commandBase.getAliases(), args[0])).collect(Collectors.toList());

                String[] realArgs  = (String[]) ArrayUtils.remove(args, 0);

                if (c.size() < 1)
                    new CommandHelp().run(realArgs);
                else
                    c.get(0).run((String[]) ArrayUtils.remove(args, 0));
                break;
        }

        System.exit(exitcode);
    }

    public static boolean containsIgnoreCase(String[] target, String maf)
    {
        return Arrays.stream(target).parallel().anyMatch(maf::equalsIgnoreCase);
    }
}
