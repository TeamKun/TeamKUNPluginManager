package net.kunmc.lab.teamkunpluginmanager.utils;

import org.bukkit.ChatColor;

public class Messages
{
    public static String getCommandNotFoundMessage()
    {
        StringBuilder result = new StringBuilder();
        result.append(ChatColor.RED + "使用可能なコマンド：\n");

        result.append("    ").append(commandMessageBuilder("install", "リポジトリまたはURLからインストールします。", "i")).append("\n");
        result.append("    ").append(commandMessageBuilder("remove", "プラグインをアンインストールします。", "rm")).append("\n");

        return result.toString();
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
