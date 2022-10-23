package net.kunmc.lab.kpm.installer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.installer.signals.PluginModifiedSignal;
import net.kunmc.lab.kpm.installer.task.tasks.dependencies.collector.DependsCollectStatus;
import net.kunmc.lab.kpm.signal.Signal;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * インストールの進捗状況を管理するクラスです。
 *
 * @param <T> インストーラの状態の型
 */
@Getter
public class InstallProgress<T extends Enum<T>, I extends AbstractInstaller<?, ?, T>>
{
    // TODO: include to abstract
    @Getter(AccessLevel.NONE)
    private static final KPMDaemon DAEMON;
    @Getter(AccessLevel.NONE)
    private static final HashMap<String, InstallProgress<?, ?>> PROGRESS_CACHES;
    @Getter(AccessLevel.NONE)
    private static final Path CACHE_DIRECTORY;

    /**
     * インストーラです。
     */
    private final I installer;

    /**
     * アップグレードされたプラグインの名前です。
     */
    private final List<String> upgraded;
    /**
     * 新規にインストールされたプラグインの名前です。
     */
    private final List<String> installed;
    /**
     * 削除されたプラグインの名前です。
     */
    private final List<String> removed;
    /**
     * 保留中としてマークされたプラグインの名前です。
     */
    private final List<String> pending;
    /**
     * インストールに使用される仮ディレクトリです。
     */
    private final Path installTempDir;

    static
    {
        DAEMON = KPMDaemon.getInstance();
        PROGRESS_CACHES = new HashMap<>();
        CACHE_DIRECTORY = DAEMON.getEnvs().getDataDirPath().resolve(".caches");

        if (!Files.exists(CACHE_DIRECTORY))
            try
            {
                Files.createDirectory(CACHE_DIRECTORY);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
    }

    /**
     * インストールに割り当てられた一意のIDです。
     */
    private final String installActionID;
    /**
     * {@link Signal} を受け取るためのリスナーです。
     */
    private final SignalHandleManager signalHandler;
    /**
     * 依存関係の解決の状態を表します。
     */
    private final DependsCollectStatus dependsCollectStatus;
    /**
     * 実行中のタスクを表します。
     */
    @Setter
    private T currentTask;

    /**
     * インストールが終了したかどうかを表します。
     */
    private boolean finished;

    private InstallProgress(@NotNull I installer,
                            @NotNull SignalHandleManager signalHandler,
                            @Nullable String id)
            throws IOException, SecurityException
    {
        this.installer = installer;
        this.signalHandler = signalHandler;

        this.upgraded = new ArrayList<>();
        this.installed = new ArrayList<>();
        this.removed = new ArrayList<>();
        this.pending = new ArrayList<>();

        this.currentTask = null;

        if (id == null)
            this.installActionID = UUID.randomUUID().toString().substring(0, 8);
        else
            this.installActionID = id;

        this.installTempDir = Files.createTempDirectory(
                CACHE_DIRECTORY,
                this.getInstallActionID()
        );

        this.dependsCollectStatus = new DependsCollectStatus(this);

        this.finished = false;

        PROGRESS_CACHES.put(this.getInstallActionID(), this);
    }

    /**
     * このインスタンスを取得します。
     *
     * @param signalHandler インストールに使用する {@link SignalHandleManager}
     * @param id            インストールに割り当てる一意のID
     * @param <P>           インストールの進捗状況の型
     * @return インスタンス
     * @throws IOException        ディレクトリの作成に失敗した場合
     * @throws SecurityException  ディレクトリの作成に失敗した場合
     * @throws ClassCastException 間違った型のキャッシュを取得しようとした場合
     */
    @SuppressWarnings("unchecked")
    public static <P extends Enum<P>, PI extends AbstractInstaller<?, ?, P>> InstallProgress<P, PI> of(
            @NotNull PI installer,
            @NotNull SignalHandleManager signalHandler,
            @Nullable String id)
            throws IOException, SecurityException
    {
        if (id == null)
            return new InstallProgress<>(installer, signalHandler, null);
        else
            return (InstallProgress<P, PI>) PROGRESS_CACHES.get(id);
    }

    private void removeFromAll(@NotNull String name)
    {
        this.upgraded.remove(name);
        this.installed.remove(name);
        this.removed.remove(name);
        this.pending.remove(name);
    }

    /**
     * プラグインがアップグレードされたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#UPGRADE} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription  アップグレードされたプラグインの {@link PluginDescriptionFile}
     * @param postModifiedSignal {@link PluginModifiedSignal} をスローするかどうか
     */
    public void addUpgraded(@NotNull PluginDescriptionFile pluginDescription, boolean postModifiedSignal)
    {
        this.removeFromAll(pluginDescription.getName());

        if (postModifiedSignal)
            this.signalHandler.handleSignal(
                    this,
                    new PluginModifiedSignal(pluginDescription, PluginModifiedSignal.ModifyType.UPGRADE)
            );
        this.upgraded.add(pluginDescription.getName());
    }

    /**
     * プラグインが新規にインストールされたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#ADD} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription  新規にインストールされたプラグインの {@link PluginDescriptionFile}
     * @param postModifiedSignal {@link PluginModifiedSignal} をスローするかどうか
     */
    public void addInstalled(@NotNull PluginDescriptionFile pluginDescription, boolean postModifiedSignal)
    {
        this.removeFromAll(pluginDescription.getName());

        if (postModifiedSignal)
            this.signalHandler.handleSignal(
                    this,
                    new PluginModifiedSignal(pluginDescription, PluginModifiedSignal.ModifyType.ADD)
            );
        this.installed.add(pluginDescription.getName());
    }

    /**
     * プラグインが削除されたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#REMOVE} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription  削除されたプラグインの {@link PluginDescriptionFile}
     * @param postModifiedSignal {@link PluginModifiedSignal} をスローするかどうか
     */
    public void addRemoved(@NotNull PluginDescriptionFile pluginDescription, boolean postModifiedSignal)
    {
        this.removeFromAll(pluginDescription.getName());

        if (postModifiedSignal)
            this.signalHandler.handleSignal(
                    this,
                    new PluginModifiedSignal(pluginDescription, PluginModifiedSignal.ModifyType.REMOVE)
            );
        this.removed.add(pluginDescription.getName());
    }

    /**
     * プラグインがアップグレードされたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#UPGRADE} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription アップグレードされたプラグインの {@link PluginDescriptionFile}
     * @see #addUpgraded(PluginDescriptionFile, boolean)
     */
    public void addUpgraded(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.addUpgraded(pluginDescription, true);
    }

    /**
     * プラグインが新規にインストールされたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#ADD} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription 新規にインストールされたプラグインの {@link PluginDescriptionFile}
     * @see #addInstalled(PluginDescriptionFile, boolean)
     */
    public void addInstalled(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.addInstalled(pluginDescription, true);
    }

    /**
     * プラグインが削除されたとしてマークします。
     * {@link PluginModifiedSignal#getModifyType()} が {@link PluginModifiedSignal.ModifyType#REMOVE} の {@link PluginModifiedSignal} をスローします。
     *
     * @param pluginDescription 削除されたプラグインの {@link PluginDescriptionFile}
     * @see #addRemoved(PluginDescriptionFile, boolean)
     */
    public void addRemoved(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.addRemoved(pluginDescription, true);
    }

    /**
     * プラグインが保留されたとしてマークします。
     *
     * @param pluginName 保留されたプラグインの名前
     */
    public void addPending(@NotNull String pluginName)
    {
        this.removeFromAll(pluginName);

        this.pending.add(pluginName);
    }

    /**
     * アップグレードされたとしてマークします。
     *
     * @param targetName アップグレードされた物の名前または識別子
     */
    public void addUpgraded(@NotNull String targetName)
    {
        this.removeFromAll(targetName);

        this.upgraded.add(targetName);
    }

    /**
     * 新規にインストールされたとしてマークします。
     *
     * @param targetName 新規にインストールされた物の名前または識別子
     */
    public void addInstalled(@NotNull String targetName)
    {
        this.removeFromAll(targetName);

        this.installed.add(targetName);
    }

    /**
     * 削除されたとしてマークします。
     *
     * @param targetName 削除された物の名前または識別子
     */
    public void addRemoved(@NotNull String targetName)
    {
        this.removeFromAll(targetName);

        this.removed.add(targetName);
    }

    /**
     * インストールが完了したとしてマークし、インストールに使用された仮ディレクトリを削除します。
     */
    public void finish()
    {
        try
        {
            FileUtils.forceDelete(this.installTempDir.toFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        PROGRESS_CACHES.remove(this.getInstallActionID());

        this.finished = true;
    }
}
