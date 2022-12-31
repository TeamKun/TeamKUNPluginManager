package net.kunmc.lab.kpm.interfaces.installer;

import net.kunmc.lab.kpm.interfaces.installer.signals.PluginModifiedSignal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

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

    I getInstaller();

    java.util.List<String> getUpgraded();

    java.util.List<String> getInstalled();

    java.util.List<String> getRemoved();

    java.util.List<String> getPending();

    java.nio.file.Path getInstallTempDir();

    String getInstallActionID();

    net.kunmc.lab.kpm.signal.SignalHandleManager getSignalHandler();

    DependsCollectStatus getDependsCollectStatus();

    T getCurrentTask();

    void setCurrentTask(T currentTask);

    boolean isFinished();
}
