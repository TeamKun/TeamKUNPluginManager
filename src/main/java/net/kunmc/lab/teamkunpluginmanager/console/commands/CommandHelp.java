package net.kunmc.lab.teamkunpluginmanager.console.commands;

import net.kunmc.lab.teamkunpluginmanager.console.PluginManagerConsole;
import net.kunmc.lab.teamkunpluginmanager.console.utils.Color;
import net.kunmc.lab.teamkunpluginmanager.console.utils.Property;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHelp implements CommandBase
{

    @Override
    public String getName()
    {
        return "help";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"?"};
    }

    @Override
    public int run(String... args)
    {
        if (args.length == 1)
        {
            List<CommandBase> c = Arrays.stream(PluginManagerConsole.commands).parallel().
                    filter(commandBase -> commandBase.getName().equalsIgnoreCase(args[0]) ||
                            PluginManagerConsole.containsIgnoreCase(commandBase.getAliases(), args[0]))
                    .collect(Collectors.toList());

            if (c.size() < 1)
            {
                System.out.println(Color.RED + "E: コマンドが見つかりませんでした。");
                return 1;
            }

            c.get(0).printHelp();
            return 0;
        }
        printHelp();
        return 0;
    }

    @Override
    public void printHelp()
    {
        StringBuilder hlp = new StringBuilder();

        String jarFileName = System.getProperty("java.class.path");
        String sep = System.getProperty("file.separator");

        jarFileName = jarFileName.substring(jarFileName.lastIndexOf(sep) + 1);

        hlp.append("TeamKunPluginManager ").append(Property.getProperty().getProperty("console.version")).append("\n");
        hlp.append("使用法: java -jar ").append(jarFileName).append(" <コマンド> [オプション]\n");
        hlp.append("\n");
        hlp.append("TeamKunPluginManager は、管理、インストール、プラグインに関する情報を問い合わせるコマンドを")
                .append("提供するプラグインマネージャです。Bukkitのプラグインとしても動作します。\n");
        hlp.append("\n");
        hlp.append("最も使用されているコマンド: \n");
        hlp.append("  install   - プラグインをインストール\n");
        hlp.append("  uninstall - プラグインをアンインストール\n");
        hlp.append("  info      - プラグインに関する情報を問い合わせ\n");
        hlp.append("  update    - 利用可能プラグインの一覧を更新\n");
        hlp.append("\n");
        hlp.append("コマンドに関する使用法等の詳細は、使用法: java -jar ").append(jarFileName).append(" <コマンド> --help\n");
        hlp.append("を参照してください。\n");
        System.out.println(hlp.toString());
    }
}
