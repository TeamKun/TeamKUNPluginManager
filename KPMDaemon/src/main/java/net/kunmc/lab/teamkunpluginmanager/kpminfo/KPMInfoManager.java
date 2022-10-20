package net.kunmc.lab.teamkunpluginmanager.kpminfo;

import net.kunmc.lab.teamkunpluginmanager.KPMDaemon;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * KPM情報ファイルを管理するクラスです。
 */
public class KPMInfoManager
{
    private final KPMDaemon daemon;
    private final HashMap<String, KPMInformationFile> lookupNames;

    public KPMInfoManager(KPMDaemon daemon)
    {
        this.daemon = daemon;
        this.lookupNames = new HashMap<>();
    }

    /**
     * KPM情報ファイルを読み込み追加します。
     *
     * @param path            KPM情報ファイルのパス
     * @param descriptionFile プラグインの説明ファイル
     * @return 読み込みに成功した場合はKPM情報ファイル、失敗した場合はnull
     */
    @Nullable
    public KPMInformationFile loadInfo(@NotNull Path path, @NotNull PluginDescriptionFile descriptionFile)
    {
        KPMInformationFile info = null;
        try
        {
            info = KPMInfoParser.load(path);
            this.lookupNames.put(descriptionFile.getName(), info);
        }
        catch (InvalidInformationFileException e)
        {
            this.daemon.getLogger().warn(
                    "The plugin " + descriptionFile.getName() + " has invalid KPM information file: " + e.getMessage());
            this.daemon.getLogger().warn("The plugin's KPM information will be ignored.");
        }
        catch (FileNotFoundException ignored)
        {
        }

        return info;
    }

    /**
     * プラグインのKPM情報ファイルを取得します。
     *
     * @param plugin プラグイン
     * @return プラグインのKPM情報ファイル
     */
    @Nullable
    public KPMInformationFile getInfo(@NotNull Plugin plugin)
    {
        return this.lookupNames.get(plugin.getDescription().getName());
    }

    /**
     * プラグインのKPM情報ファイルを取得します。
     *
     * @param pluginName プラグイン名
     * @return プラグインのKPM情報ファイル
     */

    @Nullable
    public KPMInformationFile getInfo(@NotNull String pluginName)
    {
        return this.lookupNames.get(pluginName);
    }

    /**
     * プラグインのKPM情報ファイルを削除します。
     *
     * @param plugin プラグイン
     */
    public void removeInfo(@NotNull Plugin plugin)
    {
        this.lookupNames.remove(plugin.getDescription().getName());
    }
}
