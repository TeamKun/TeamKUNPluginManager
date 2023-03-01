package org.kunlab.kpm.installer.interfaces;

import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.installer.signals.PluginModifiedSignal;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.signal.SignalHandleManager;
import org.kunlab.kpm.task.interfaces.dependencies.collector.DependsCollectStatus;

import java.nio.file.Path;
import java.util.List;

/**
 * インストールの進捗状況を管理するクラスです。
 *
 * @param <T> インストーラの状態の型
 */
public interface InstallProgress<T extends Enum<T>, I extends PluginInstaller<?, ?, T>>
{
    /**
     * プラグインがアップグレードされたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#UPGRADE} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription  アップグレードされたプラグインの {@link PluginDescriptionFile}
     * @param postModifiedSignal {@link PluginModifiedSignal} をスローするかどうか
     */
    void addUpgraded(@NotNull PluginDescriptionFile pluginDescription, boolean postModifiedSignal);

    /**
     * プラグインが新規にインストールされたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#ADD} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription  新規にインストールされたプラグインの {@link PluginDescriptionFile}
     * @param postModifiedSignal {@link PluginModifiedSignal} をスローするかどうか
     */
    void addInstalled(@NotNull PluginDescriptionFile pluginDescription, boolean postModifiedSignal);

    /**
     * プラグインが削除されたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#REMOVE} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription  削除されたプラグインの {@link PluginDescriptionFile}
     * @param postModifiedSignal {@link PluginModifiedSignal} をスローするかどうか
     */
    void addRemoved(@NotNull PluginDescriptionFile pluginDescription, boolean postModifiedSignal);

    /**
     * プラグインがアップグレードされたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#UPGRADE} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription アップグレードされたプラグインの {@link PluginDescriptionFile}
     * @see #addUpgraded(PluginDescriptionFile, boolean)
     */
    void addUpgraded(@NotNull PluginDescriptionFile pluginDescription);

    /**
     * プラグインが新規にインストールされたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#ADD} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription 新規にインストールされたプラグインの {@link PluginDescriptionFile}
     * @see #addInstalled(PluginDescriptionFile, boolean)
     */
    void addInstalled(@NotNull PluginDescriptionFile pluginDescription);

    /**
     * プラグインが削除されたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#REMOVE} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription 削除されたプラグインの {@link PluginDescriptionFile}
     * @see #addRemoved(PluginDescriptionFile, boolean)
     */
    void addRemoved(@NotNull PluginDescriptionFile pluginDescription);

    /**
     * プラグインが保留されたとしてマークします。
     *
     * @param pluginName 保留されたプラグインの名前
     */
    void addPending(@NotNull String pluginName);

    /**
     * アップグレードされたとしてマークします。
     *
     * @param targetName アップグレードされた物の名前または識別子
     */
    void addUpgraded(@NotNull String targetName);

    /**
     * 新規にインストールされたとしてマークします。
     *
     * @param targetName 新規にインストールされた物の名前または識別子
     */
    void addInstalled(@NotNull String targetName);

    /**
     * 削除されたとしてマークします。
     *
     * @param targetName 削除された物の名前または識別子
     */
    void addRemoved(@NotNull String targetName);

    /**
     * インストールが完了したとしてマークし、インストールに使用された仮ディレクトリを削除します。
     */
    void finish();

    /**
     * インストーラです。
     */
    I getInstaller();

    /**
     * アップグレードされたプラグインの名前です。
     */
    List<String> getUpgraded();

    /**
     * 新規にインストールされたプラグインの名前です。
     */
    List<String> getInstalled();

    /**
     * 削除されたプラグインの名前です。
     */
    List<String> getRemoved();

    /**
     * 保留中としてマークされたプラグインの名前です。
     */
    List<String> getPending();

    /**
     * インストールに使用される仮ディレクトリです。
     */
    Path getInstallTempDir();

    /**
     * インストールに割り当てられた一意のIDです。
     */
    String getInstallActionID();

    /**
     * {@link Signal} を受け取るためのリスナーです。
     */
    SignalHandleManager getSignalHandler();

    /**
     * 依存関係の解決の状態を表します。
     */
    DependsCollectStatus getDependsCollectStatus();

    /**
     * 実行中のタスクを表します。
     */
    T getCurrentTask();

    /**
     * 実行中のタスクを表します。
     */
    void setCurrentTask(T currentTask);

    /**
     * インストールが終了したかどうかを表します。
     */
    boolean isFinished();
}
