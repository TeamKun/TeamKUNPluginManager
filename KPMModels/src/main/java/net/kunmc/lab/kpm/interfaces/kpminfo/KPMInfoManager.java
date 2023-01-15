package net.kunmc.lab.kpm.interfaces.kpminfo;

import net.kunmc.lab.kpm.kpminfo.InvalidInformationFileException;
import net.kunmc.lab.kpm.kpminfo.KPMInformationFile;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * KPM情報ファイルを管理するクラスです。
 */
public interface KPMInfoManager
{
    /**
     * KPM情報ファイルを読み込み追加します。
     *
     * @param path            KPM情報ファイルのパス
     * @param descriptionFile プラグインの説明ファイル
     * @return 読み込みに成功した場合はKPM情報ファイル、失敗した場合はnull
     * @throws InvalidInformationFileException KPM情報ファイルが不正な場合
     * @throws FileNotFoundException           KPM情報ファイルが見つからない場合
     */
    @Nullable KPMInformationFile loadInfo(@NotNull Path path, @NotNull PluginDescriptionFile descriptionFile) throws
            FileNotFoundException, InvalidInformationFileException;

    /**
     * プラグインのKPM情報ファイルを取得します。
     *
     * @param plugin プラグイン
     * @return プラグインのKPM情報ファイル
     */
    @Nullable KPMInformationFile getInfo(@NotNull Plugin plugin);

    /**
     * プラグインのKPM情報ファイルを取得します。
     *
     * @param pluginName プラグイン名
     * @return プラグインのKPM情報ファイル
     */

    @Nullable KPMInformationFile getInfo(@NotNull String pluginName);

    /**
     * プラグインのKPM情報ファイルを取得するか読み込みます。
     *
     * @param plugin プラグイン
     * @return プラグインのKPM情報ファイル
     */
    @Nullable KPMInformationFile getOrLoadInfo(@NotNull Plugin plugin);

    /**
     * プラグインが KPM情報ファイルを持っているかどうかを取得します。
     *
     * @param plugin プラグイン名
     * @return プラグインが KPM情報ファイルを持っているかどうか
     */
    boolean hasInfo(@NotNull Plugin plugin);

    /**
     * プラグインが KPM情報ファイルを持っているかどうかを取得します。
     *
     * @param pluginFile プラグインのファイル
     * @return プラグインが KPM情報ファイルを持っているかどうか
     */
    boolean hasInfo(@NotNull Path pluginFile);

    /**
     * プラグインのKPM情報ファイルを削除します。
     *
     * @param plugin プラグイン
     */
    void removeInfo(@NotNull Plugin plugin);
}
