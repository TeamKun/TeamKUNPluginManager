package net.kunmc.lab.teamkunpluginmanager.utils;

import net.kunmc.lab.teamkunpluginmanager.plugin.DependencyTree;
import org.bukkit.ChatColor;

import java.util.List;

public class Messages
{
    public static String getCommandNotFoundMessage()
    {

        return ChatColor.RED + "使用可能なコマンド: \n" +
                "    " + commandMessageBuilder("install", "リポジトリまたはURLからインストールします。", "i") + "\n" +
                "    " + commandMessageBuilder("remove", "プラグインをアンインストールします。", "rm", "uninstall") + "\n" +
                "    " + commandMessageBuilder("autoremove", "いらないプラグインを自動で削除します。") + "\n" +
                "    " + commandMessageBuilder("update", "既知プラグインデータセットをアップデートします。") + "\n" +
                "    " + commandMessageBuilder("status", "現在の状態を表示します。") + "\n" +
                "    " + commandMessageBuilder("info", "プラグインの情報を取得します。") + "\n" +
                "    " + commandMessageBuilder("fix", "エラーを修復します。\n" + ChatColor.YELLOW +
                "          メッセージがあった場合のみ実行してください。") + "\n" +
                "    " + commandMessageBuilder("clean", "不要になったプラグインデータを削除します。") + "\n" +
                "    " + commandMessageBuilder("export", "プラグインをエクスポートします。") + "\n" +
                "    " + commandMessageBuilder("import", "エクスポートしたファイルからインポートします。") + "\n" +
                "    " + commandMessageBuilder("register", "トークンをセットします。") + "\n";
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

        return ChatColor.BLUE + "以下のプラグインがインストールされていますが、もう必要とされていません:\n" +
                ChatColor.GREEN + "  " + String.join(" ", rmble) + "\n" +
                ChatColor.BLUE + "これを削除するには、'/kpm autoremove' を利用してください。";
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
