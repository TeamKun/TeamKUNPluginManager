package net.kunmc.lab.teamkunpluginmanager.plugin;

import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.utils.PluginUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class Installer
{
    /**
     * 削除可能なデータフォルダを取得
     *
     * @return データフォルダ
     */
    public static String[] getRemovableDataDirs()
    {
        try
        {
            //ignoreされているものを全て取得
            List<String> bb = TeamKunPluginManager.getPlugin().getPluginConfig().getStringList("ignore");

            return Arrays.stream(Objects.requireNonNull(new File("plugins/").listFiles(File::isDirectory))) //plugins/の中のフォルダを全取得
                    .map(File::getName)                               //Stream<File> => Stream<String> ファイルの名前
                    .filter(file -> !PluginUtil.isPluginLoaded(file)) //プラグインフォルダが使用されていたら除外
                    .filter(file -> !bb.contains(file))               //除外リスト似合った場合はreturn
                    .toArray(String[]::new);                          //結果を全てreturn

        }
        catch (Exception e) //例外が発生した場合は空return
        {
            return new String[]{};
        }
    }

    /**
     * プラグインデータフォルダを削除
     *
     * @param name 対象
     * @return 合否
     */
    public static boolean clean(String name)
    {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);

        if (PluginUtil.isPluginLoaded(name))
            return false;  //プラグインがイネーブルの時、プロセスロックが掛かる

        if (TeamKunPluginManager.getPlugin().getPluginConfig().getStringList("ignore").stream()
                .anyMatch(s -> s.equalsIgnoreCase(name))) // 保護されていたら除外
            return false;

        if (plugin != null)
        {
            try
            {
                FileUtils.forceDelete(plugin.getDataFolder());
                return true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }

        }

        try
        {
            Arrays.stream(Objects.requireNonNull(new File("plugins/").listFiles(File::isDirectory)))  //plugins/の中のフォルダを全取得
                    .filter(file -> file.getName().equalsIgnoreCase(name)) //一致するフォルダを取得
                    .forEach(file -> {
                        try
                        {
                            //強制削除
                            FileUtils.forceDelete(file);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    });
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
