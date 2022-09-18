package net.kunmc.lab.teamkunpluginmanager.plugin.meta;

import lombok.Value;
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
     * プラグインのインストール者です。
     */
    @NotNull
    InstallOperator installedBy;
    /**
     * プラグインが依存関係であるかどうかを表します。
     */
    boolean isDependency;
    /**
     * プラグインのインストールに使用した解決クエリです。
     */
    @Nullable
    String resolveQuery;

    /**
     * プラグインがいつインストールされたかを表します。
     */
    long installedAt;

    /**
     * 依存しているプラグインのリストです。
     */
    @NotNull
    List<DependencyNode> dependedBy;
    /**
     *
     */
    @NotNull
    List<DependencyNode> dependsOn;

}
