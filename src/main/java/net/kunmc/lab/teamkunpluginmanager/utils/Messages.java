package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import org.bukkit.ChatColor;

import java.util.List;

public class Messages
{
    public static String getErrorMessage()
    {
        if (!DependencyTree.isErrors())
            return "";
        return ChatColor.RED + "重大なエラーが検出されました。/kpm fix で修正を行ってください。";
    }

    public static String getUnInstallableMessage()
    {
        List<String> rmble = DependencyTree.unusedPlugins();

        if (rmble.isEmpty())
            return "";

        return ChatColor.BLUE + "以下のプラグインがインストールされていますが、もう必要とされていません:\n" +
                ChatColor.GREEN + "  " + String.join(" ", rmble) + "\n" +
                ChatColor.BLUE + "これを削除するには、'/kpm autoremove' を利用してください。";
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

    public static String keyValue(String property, String value)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + ChatColor.DARK_GREEN + value;
    }

    public static String keyValueYesNo(String property, boolean a)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + (a ? ChatColor.DARK_GREEN + "はい": ChatColor.RED + "いいえ");
    }

    public static String keyValue(String property, Object obj)
    {
        return ChatColor.GREEN + property + ChatColor.WHITE + ": " + obj.toString();
    }

    public enum ModifyType
    {
        ADD,
        REMOVE,
        MODIFY
    }

}
