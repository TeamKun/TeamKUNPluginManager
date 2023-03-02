package org.kunlab.kpm.task.interfaces.dependencies.collector;

import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.task.interfaces.dependencies.DependencyElement;

import java.util.List;

/**
 * 依存関係解決の状態を表すクラスです。
 */
public interface DependsCollectStatus
{
    /**
     * 検出された依存先プラグインを追加します。
     *
     * @param dependencyName 検出されたプラグイン名
     */
    void addDependency(@NotNull String dependencyName);

    /**
     * 指定された依存先プラグインが取得されたかどうかを返します。
     *
     * @param dependencyName 依存関係名前
     * @return 取得されている場合はtrue、そうでない場合はfalse
     */
    boolean isCollected(@NotNull String dependencyName);

    /**
     * 依存関係の解決にエラーが発生したかどうかを返します。
     *
     * @return エラーが発生している場合はtrue、そうでない場合はfalse
     */
    boolean isErrors();

    /**
     * 依存関係が解決されたときに呼び出します。
     *
     * @param dependencyName    依存関係名
     * @param dependencyElement 依存関係要素
     */
    void onCollect(@NotNull String dependencyName, DependencyElement dependencyElement);

    /**
     * 解決された依存関係をすべて取得します。
     *
     * @return 依存関係要素のリスト
     */
    List<DependencyElement> getCollectedDependencies();

    /**
     * 解決に失敗した依存関係をすべて取得します。
     *
     * @return 依存関係要素のリスト
     */
    List<String> getCollectFailedDependencies();

    /**
     * 割り当てられたインストールIDです。
     */
    String getInstallId();

    /**
     * 依存関係解決の対象のプラグイン名です。
     */
    String getPluginName();

    /**
     * 依存関係解決の対象のプラグイン名です。
     */
    void setPluginName(String pluginName);
}
