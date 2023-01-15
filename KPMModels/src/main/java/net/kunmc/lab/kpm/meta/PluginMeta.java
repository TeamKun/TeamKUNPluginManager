package net.kunmc.lab.kpm.meta;

import lombok.Value;
import net.kunmc.lab.kpm.enums.metadata.InstallOperator;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * プラグインのメタ情報を表します。
 */
@Value
public class PluginMeta
{
    /**
     * プラグインの名前です。
     */
    @NotNull
    String name;
    /**
     * プラグインのバージョンです。
     */
    @NotNull
    String version;
    /**
     * プラグインのロードタイミングです。
     */
    @NotNull
    PluginLoadOrder loadTiming;

    /**
     * プラグインのインストール者です。
     */
    @NotNull
    InstallOperator installedBy;
    /**
     * プラグインが依存関係であるかどうかを表します。
     */
    boolean isDependency;
    /**
     * プラグインの解決に使用するクエリです。
     * このクエリは, アップグレード機能で使用されます。
     */
    @Nullable
    String resolveQuery;

    /**
     * プラグインがいつインストールされたかを表します。
     */
    long installedAt;
    /**
     * プラグインの作者のリストです。
     */
    @NotNull
    List<String> authors;

    /**
     * 依存されているプラグインのリストです。
     */
    @NotNull
    List<DependencyNode> dependedBy;
    /**
     * 依存しているプラグインのリストです。
     */
    @NotNull
    List<DependencyNode> dependsOn;
}
