package net.kunmc.lab.teamkunpluginmanager.console.commands;

import net.kunmc.lab.teamkunpluginmanager.console.commands.stracture.CommandBase;
import net.kunmc.lab.teamkunpluginmanager.console.utils.Property;

import java.util.Properties;

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
        return new String[] {"?"};
    }

    @Override
    public int run(String... args)
    {
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
        hlp.append("使用法: java -jar ").append(jarFileName).append(" <コマンド> [オプション]");
        hlp.append("\n");
        hlp.append("TeamKunPluginManager は、管理、インストール、プラグインに関する情報を問い合わせるコマンドを\n")
                .append("提供するプラグインマネージャです。Bukkitのプラグインとしても動作します。");
        hlp.append("\n");
        hlp.append("最も使用されているコマンド: \n");
        hlp.append("  install   - プラグインをインストール\n");
        hlp.append("  uninstall - プラグインをアンインストール\n");
        hlp.append("  info      - プラグインに関する情報を問い合わせ\n");
        hlp.append("  update    - 利用可能プラグインの一覧を更新\n");
        hlp.append("\n");
        hlp.append("コマンドに関する使用法等の詳細は、使用法: java -jar ").append(jarFileName).append(" <コマンド> --help\n");
        hlp.append("を参照してください。");
        System.out.println(hlp.toString());
    }
}
