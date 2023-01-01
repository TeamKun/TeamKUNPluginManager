package net.kunmc.lab.kpm.installer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.kpm.interfaces.installer.InstallProgress;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.interfaces.installer.PluginInstaller;
import net.kunmc.lab.kpm.interfaces.installer.signals.PluginModifiedSignal;
import net.kunmc.lab.kpm.interfaces.task.tasks.dependencies.collector.DependsCollectStatus;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.task.tasks.dependencies.collector.DependsCollectStatusImpl;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
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

@Getter
public class InstallProgressImpl<T extends Enum<T>, I extends PluginInstaller<?, ?, T>> implements InstallProgress<T, I>
{
    @Getter(AccessLevel.NONE)
    private static final HashMap<String, InstallProgress<? extends Enum<?>, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>>>> PROGRESS_CACHES;

    static
    {
        PROGRESS_CACHES = new HashMap<>();
    }

    private final I installer;
    private final List<String> upgraded;
    private final List<String> installed;
    private final List<String> removed;
    private final List<String> pending;
    private final Path installTempDir;
    private final String installActionID;
    private final SignalHandleManager signalHandler;
    private final DependsCollectStatus dependsCollectStatus;
    @Setter
    private T currentTask;

    private boolean finished;

    /**
     * このインスタンスを取得します。
     *
     * @param signalHandler インストールに使用する {@link SignalHandleManager}
     * @param id            インストールに割り当てる一意のID
     * @throws IOException        ディレクトリの作成に失敗した場合
     * @throws SecurityException  ディレクトリの作成に失敗した場合
     * @throws ClassCastException 間違った型のキャッシュを取得しようとした場合
     */
    private InstallProgressImpl(@NotNull I installer,
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

        Path cacheDir = installer.getRegistry().getEnvironment().getDataDirPath().resolve(".caches");

        this.installTempDir = Files.createTempDirectory(
                cacheDir,
                this.getInstallActionID()
        );

        this.dependsCollectStatus = new DependsCollectStatusImpl((InstallProgress<? extends Enum<?>, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>>>) this);

        this.finished = false;

        PROGRESS_CACHES.put(this.getInstallActionID(), (InstallProgress<? extends Enum<?>, ? extends PluginInstaller<? extends InstallerArgument, ? extends Enum<?>, ? extends Enum<?>>>) this);
    }

    @SuppressWarnings("unchecked")
    public static <P extends Enum<P>, PI extends PluginInstaller<?, ?, P>> InstallProgress<P, PI> of(
            @NotNull PI installer,
            @NotNull SignalHandleManager signalHandler,
            @Nullable String id)
            throws IOException, SecurityException
    {
        if (id == null)
            return new InstallProgressImpl<>(installer, signalHandler, null);
        else
            return (InstallProgressImpl<P, PI>) PROGRESS_CACHES.get(id);
    }

    private void removeFromAll(@NotNull String name)
    {
        this.upgraded.remove(name);
        this.installed.remove(name);
        this.removed.remove(name);
        this.pending.remove(name);
    }

    @Override
    public void addUpgraded(@NotNull PluginDescriptionFile pluginDescription, boolean postModifiedSignal)
    {
        this.removeFromAll(pluginDescription.getName());

        if (postModifiedSignal)
            this.signalHandler.handleSignal(new PluginModifiedSignal(pluginDescription, PluginModifiedSignal.ModifyType.UPGRADE));
        this.upgraded.add(pluginDescription.getName());
    }

    @Override
    public void addInstalled(@NotNull PluginDescriptionFile pluginDescription, boolean postModifiedSignal)
    {
        this.removeFromAll(pluginDescription.getName());

        if (postModifiedSignal)
            this.signalHandler.handleSignal(new PluginModifiedSignal(pluginDescription, PluginModifiedSignal.ModifyType.ADD));
        this.installed.add(pluginDescription.getName());
    }

    @Override
    public void addRemoved(@NotNull PluginDescriptionFile pluginDescription, boolean postModifiedSignal)
    {
        this.removeFromAll(pluginDescription.getName());

        if (postModifiedSignal)
            this.signalHandler.handleSignal(new PluginModifiedSignal(pluginDescription, PluginModifiedSignal.ModifyType.REMOVE));
        this.removed.add(pluginDescription.getName());
    }

    @Override
    public void addUpgraded(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.addUpgraded(pluginDescription, true);
    }

    @Override
    public void addInstalled(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.addInstalled(pluginDescription, true);
    }

    @Override
    public void addRemoved(@NotNull PluginDescriptionFile pluginDescription)
    {
        this.addRemoved(pluginDescription, true);
    }

    @Override
    public void addPending(@NotNull String pluginName)
    {
        this.removeFromAll(pluginName);

        this.pending.add(pluginName);
    }

    @Override
    public void addUpgraded(@NotNull String targetName)
    {
        this.removeFromAll(targetName);

        this.upgraded.add(targetName);
    }

    @Override
    public void addInstalled(@NotNull String targetName)
    {
        this.removeFromAll(targetName);

        this.installed.add(targetName);
    }

    @Override
    public void addRemoved(@NotNull String targetName)
    {
        this.removeFromAll(targetName);

        this.removed.add(targetName);
    }

    @Override
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