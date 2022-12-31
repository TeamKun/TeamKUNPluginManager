package net.kunmc.lab.kpm.interfaces.task.tasks.dependencies;

import net.kunmc.lab.kpm.kpminfo.KPMInformationFile;
import org.bukkit.plugin.PluginDescriptionFile;

import java.nio.file.Path;

/**
 * 依存関係 系のタスクで使用される、概念的な依存関係を表すクラスです。
 */
public interface DependencyElement
{
    /**
     * 依存関係の名前です。
     */
    String getPluginName();

    /**
     * 依存関係プラグインがあるのパスです。
     */
    Path getPluginPath();

    /**
     * 依存関係のプラグイン情報ファイルです。
     */
    PluginDescriptionFile getPluginDescription();

    /**
     * 依存関係のKPM情報ファイルです。
     */
    KPMInformationFile getKpmInfoFile();

    /**
     * 依存関係の解決に使用したクエリです。
     */
    String getQuery();
}
