package net.kunmc.lab.kpm.installer.impls.uninstall;

import lombok.Builder;
import lombok.Data;
import net.kunmc.lab.kpm.interfaces.installer.InstallerArgument;
import net.kunmc.lab.kpm.task.tasks.uninstall.signals.PluginIsDependencySignal;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * アンインストールの引数を格納するクラスです。
 */
@Data
@Builder
public class UninstallArgument implements InstallerArgument
{
    /**
     * アンインストールするプラグインの名前です。
     * {@link #plugins} またはこのフィールドのどちらかが指定されている必要があります。
     */
    @Nullable
    @Unmodifiable
    private final List<String> pluginNames;

    /**
     * アンインストールするプラグインのインスタンスです。
     * {@link #pluginNames} またはこのフィールドのどちらかが指定されている必要があります。
     */
    @Nullable
    @Unmodifiable
    private final List<Plugin> plugins;

    /**
     * 除外チェックをスキップするかどうかのフラグです。
     */
    @Builder.Default
    private final boolean skipExcludeChecks = false;

    /**
     * 依存関係チェックをスキップするかどうかのフラグです。
     */
    @Builder.Default
    private final boolean skipDependencyChecks = false;

    /**
     * 強制アンインストールを行うかどうかのフラグです。
     * デフォルトでは, 状況に応じたシグナルが送信され, 選択を促されます。
     * このフラグが true の場合, シグナルは送信されますが, シグナルの選択は無視され, アンインストールが行われます。
     */
    @Builder.Default
    private final boolean forceUninstall = false;

    /**
     * 自動でアンインストールを行います。
     */
    @Builder.Default
    private final boolean autoConfirm = false;

    /**
     * 被依存関係にあるプラグインが見つかった場合の処理方法のデフォルト設定です。
     * この設定は, 関連するシグナルによって上書きされる可能性があります。
     */
    @Builder.Default
    @NotNull
    private final PluginIsDependencySignal.Operation onDependencyFound =
            PluginIsDependencySignal.Operation.CANCEL;

    public static UninstallArgumentBuilder builder(Plugin plugin)
    {
        return new UninstallArgumentBuilder()
                .plugins(Collections.singletonList(plugin));
    }

    public static UninstallArgumentBuilder builder(String pluginName)
    {
        return new UninstallArgumentBuilder()
                .pluginNames(Collections.singletonList(pluginName));
    }

    public static UninstallArgumentBuilder builder(Plugin... plugins)
    {
        return new UninstallArgumentBuilder()
                .plugins(Arrays.asList(plugins));
    }

    public static UninstallArgumentBuilder builder(String... pluginNames)
    {
        return new UninstallArgumentBuilder()
                .pluginNames(Arrays.asList(pluginNames));
    }
}
