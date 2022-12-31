package net.kunmc.lab.kpm.interfaces.installer.loader;

import net.kunmc.lab.kpm.installer.loader.PluginLoadResult;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface PluginLoader
{
    /**
     * プラグインを読み込みます。
     *
     * @param pluginPath プラグインのパス
     * @return プラグインの読み込み結果
     */
    PluginLoadResult loadPlugin(@NotNull Path pluginPath);

    /**
     * プラグインをアンロードします。
     *
     * @param plugin アンロードするプラグイン
     */
    void unloadPlugin(@NotNull Plugin plugin);

    /**
     * プラグインを再読み込みします。
     *
     * @param plugin 再読み込みするプラグイン
     */
    void reloadPlugin(Plugin plugin);
}
