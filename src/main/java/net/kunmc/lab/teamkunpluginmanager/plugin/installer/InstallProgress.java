package net.kunmc.lab.teamkunpluginmanager.plugin.installer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.teamkunpluginmanager.TeamKunPluginManager;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.signals.PluginModifiedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.dependencies.collector.DependsCollectStatus;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandleManager;
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
public class InstallProgress<T extends Enum<T>>
{
    @Getter(AccessLevel.NONE)
    private static final HashMap<String, InstallProgress<?>> PROGRESS_CACHES;
    @Getter(AccessLevel.NONE)
    private static final Path CACHE_DIRECTORY;

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
        PROGRESS_CACHES = new HashMap<>();
        CACHE_DIRECTORY = TeamKunPluginManager.getPlugin().getDataFolder().toPath().resolve(".cache");

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
     * {@link InstallerSignal} を受け取るためのリスナーです。
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

    private InstallProgress(@NotNull SignalHandleManager signalHandler, @Nullable String id) throws IOException, SecurityException
    {
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

        PROGRESS_CACHES.put(this.getInstallActionID(), this);
    }

    /**
     * このインスタンスを取得します。
     *
     * @param signalHandler インストールに使用する {@link SignalHandleManager}
     * @param id            インストールに割り当てる一意のID
     * @param <P>           インストールの進捗状況の型
     * @return インスタンス
     * @throws IOException       ディレクトリの作成に失敗した場合
     * @throws SecurityException ディレクトリの作成に失敗した場合
     */
    public static <P extends Enum<P>> InstallProgress<P> of(@NotNull SignalHandleManager signalHandler,
                                                            @Nullable String id) throws IOException, SecurityException
    {
        if (id == null)
            return new InstallProgress<>(signalHandler, null);
        else
            return (InstallProgress<P>) PROGRESS_CACHES.get(id);
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
     * @param pluginDescription アップグレードされたプラグインの {@link PluginDescriptionFile}
     */
    public void addUpgraded(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.removeFromAll(pluginDescription.getName());

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
     * @param pluginDescription 新規にインストールされたプラグインの {@link PluginDescriptionFile}
     */
    public void addInstalled(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.removeFromAll(pluginDescription.getName());

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
     * @param pluginDescription 削除されたプラグインの {@link PluginDescriptionFile}
     */
    public void addRemoved(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.removeFromAll(pluginDescription.getName());

        this.signalHandler.handleSignal(
                this,
                new PluginModifiedSignal(pluginDescription, PluginModifiedSignal.ModifyType.REMOVE)
        );
        this.removed.add(pluginDescription.getName());
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
    }
}
