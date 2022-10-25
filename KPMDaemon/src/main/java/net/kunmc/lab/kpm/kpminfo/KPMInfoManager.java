package net.kunmc.lab.kpm.kpminfo;

import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.utils.PluginUtil;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.zip.ZipFile;

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
     * @throws InvalidInformationFileException KPM情報ファイルが不正な場合
     * @throws FileNotFoundException           KPM情報ファイルが見つからない場合
     */
    @Nullable
    public KPMInformationFile loadInfo(@NotNull Path path, @NotNull PluginDescriptionFile descriptionFile) throws
            FileNotFoundException, InvalidInformationFileException
    {
        KPMInformationFile info = KPMInfoParser.load(this.daemon, path);
        this.lookupNames.put(descriptionFile.getName(), info);

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
     * プラグインのKPM情報ファイルを取得するか読み込みます。
     *
     * @param plugin プラグイン
     * @return プラグインのKPM情報ファイル
     */
    @Nullable
    public KPMInformationFile getOrLoadInfo(@NotNull Plugin plugin)
    {
        KPMInformationFile info = this.getInfo(plugin);
        if (info != null)
            return info;

        try
        {
            return this.loadInfo(PluginUtil.getFile(plugin).toPath(), plugin.getDescription());
        }
        catch (FileNotFoundException | InvalidInformationFileException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * プラグインが KPM情報ファイルを持っているかどうかを取得します。
     *
     * @param plugin プラグイン名
     * @return プラグインが KPM情報ファイルを持っているかどうか
     */
    public boolean hasInfo(@NotNull Plugin plugin)
    {
        if (this.lookupNames.containsKey(plugin.getDescription().getName()))
            return true;

        return this.hasInfo(PluginUtil.getFile(plugin).toPath());
    }

    /**
     * プラグインが KPM情報ファイルを持っているかどうかを取得します。
     *
     * @param pluginFile プラグインのファイル
     * @return プラグインが KPM情報ファイルを持っているかどうか
     */
    public boolean hasInfo(@NotNull Path pluginFile)
    {
        try (ZipFile zipFile = new ZipFile(pluginFile.toFile()))
        {
            return zipFile.getEntry("kpm.info") != null;
        }
        catch (Exception e)
        {
            return false;
        }
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
