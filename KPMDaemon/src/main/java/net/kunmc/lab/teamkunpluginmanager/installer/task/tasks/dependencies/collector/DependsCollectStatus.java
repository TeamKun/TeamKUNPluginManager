package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.dependencies.collector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.dependencies.DependencyElement;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 依存関係解決の状態を表すクラスです。
 */
@Data
@AllArgsConstructor
public class DependsCollectStatus
{
    /**
     * 割り当てられたインストールIDです。
     */
    @NotNull
    private final String installId;
    @NotNull
    @Getter(AccessLevel.PRIVATE)
    private final HashMap<String, DependencyElement> enumeratedDependencies;

    /**
     * 依存関係解決の対象のプラグイン名です。
     */
    @NotNull
    private String pluginName;

    /**
     * このクラスのインスタンスを生成します。
     *
     * @param progress InstallProgress
     */
    public DependsCollectStatus(InstallProgress<?, ?> progress)
    {
        this.installId = progress.getInstallActionID();
        this.enumeratedDependencies = new HashMap<>();
        this.pluginName = "undefined-" + this.installId;
    }

    /**
     * 検出された依存先プラグインを追加します。
     *
     * @param dependencyName 検出されたプラグイン名
     */
    public void addDependency(@NotNull String dependencyName)
    {
        if (!enumeratedDependencies.containsKey(dependencyName))
            enumeratedDependencies.put(dependencyName, null);
    }

    /**
     * 指定された依存先プラグインが取得されたかどうかを返します。
     *
     * @param dependencyName 依存関係名前
     * @return 取得されている場合はtrue、そうでない場合はfalse
     */
    public boolean isCollected(@NotNull String dependencyName)
    {
        return enumeratedDependencies.containsKey(dependencyName);
    }

    /**
     * 依存関係の解決にエラーが発生したかどうかを返します。
     *
     * @return エラーが発生している場合はtrue、そうでない場合はfalse
     */
    public boolean isErrors()
    {
        return enumeratedDependencies.containsValue(null);
    }

    /**
     * 依存関係が解決されたときに呼び出します。
     *
     * @param dependencyName    依存関係名
     * @param dependencyElement 依存関係要素
     */
    public void onCollect(@NotNull String dependencyName, DependencyElement dependencyElement)
    {
        if (enumeratedDependencies.containsKey(dependencyName))
            enumeratedDependencies.put(dependencyName, dependencyElement);
    }

    /**
     * 解決された依存関係をすべて取得します。
     *
     * @return 依存関係要素のリスト
     */
    public List<DependencyElement> getCollectedDependencies()
    {
        return this.enumeratedDependencies.entrySet().stream().parallel()
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 解決に失敗した依存関係をすべて取得します。
     *
     * @return 依存関係要素のリスト
     */
    public List<String> getCollectFailedDependencies()
    {
        return this.enumeratedDependencies.entrySet().stream().parallel()
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
