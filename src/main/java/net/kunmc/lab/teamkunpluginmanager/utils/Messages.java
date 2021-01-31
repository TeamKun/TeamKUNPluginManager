package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import org.bukkit.ChatColor;

import java.util.List;

public class Messages
{
    public static String getCommandNotFoundMessage()
    {

        return ChatColor.RED + "使用可能なコマンド：\n" +
                "    " + commandMessageBuilder("install", "リポジトリまたはURLからインストールします。", "i") + "\n" +
                "    " + commandMessageBuilder("remove", "プラグインをアンインストールします。", "rm", "uninstall") + "\n" +
                "    " + commandMessageBuilder("autoremove", "いらないプラグインを自動で削除します。") + "\n" +
                "    " + commandMessageBuilder("status", "現在の状態を表示します。") + "\n";
    }

    public static String getErrorMessage()
    {
        if (!DependencyTree.isErrors())
            return "";
        return ChatColor.RED + "重大なエラーが検出されました。/kpm fix で修正を行ってください。";
    }

    public static String getUnInstallableMessage()
    {

        List<String> rmble = DependencyTree.unusedPlugins();

        if (rmble.size() == 0)
            return "";

        return ChatColor.BLUE + "以下のプラグインを削除可能です。/kpm autoremove で削除します。" + "\n" +
                ChatColor.AQUA + String.join(", ", rmble) + "\n\n";
    }

    private static String commandMessageBuilder(String label, String help, String... aliases)
    {
        return ChatColor.GREEN + label +
                (aliases.length != 0 ? ", " + String.join(", ", aliases): "") +
                ChatColor.YELLOW + " - " + help;
    }

    public static String getModifyMessage(ModifyType type, String name)
    {
        switch (type)
        {
            case ADD:
                return ChatColor.GREEN + "+ " + name;
            case MODIFY:
                return ChatColor.YELLOW + "~ " + name;
            case REMOVE:
                return ChatColor.RED + "- " + name;
            default:
                return ChatColor.GOLD + "? " + name;
        }
    }

    public static String getStatusMessage(int installed, int removed, int modified)
    {
        return ChatColor.GREEN.toString() + installed + " 追加 " + ChatColor.RED + removed + " 削除 " + ChatColor.YELLOW + modified + " 変更";
    }

    public enum ModifyType
    {
        ADD,
        REMOVE,
        MODIFY
    }

}
