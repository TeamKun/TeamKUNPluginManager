package net.kunmc.lab.kpm.task.tasks.uninstall;

import lombok.Getter;
import net.kunmc.lab.kpm.task.AbstractTaskResult;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * プラグインのアンインストールを行うタスクの結果です。
 */
@Getter
public class UnInstallResult extends AbstractTaskResult<UninstallState, UninstallErrorCause>
{
    /**
     * アンインストールされたプラグインのプラグイン情報ファイルです。
     */
    @NotNull
    private final List<PluginDescriptionFile> uninstalledPlugins;
    /**
     * 無効化されたプラグインのプラグイン情報ファイルです。
     */
    @NotNull
    private final List<PluginDescriptionFile> disabledPlugins;
    /**
     * アンロードされたプラグインのプラグイン情報ファイルと, パスです。
     */
    @NotNull
    private final Map<PluginDescriptionFile, Path> unloadedPlugins;
    /**
     * アンインストールに失敗した理由です。
     */
    @NotNull
    private final Map<UninstallErrorCause, PluginDescriptionFile> errors;

    public UnInstallResult(boolean success, @NotNull UninstallState state, @Nullable UninstallErrorCause errorCause, @NotNull List<PluginDescriptionFile> uninstalledPlugins, @NotNull List<PluginDescriptionFile> disabledPlugins, @NotNull Map<PluginDescriptionFile, Path> unloadedPlugins, @NotNull Map<UninstallErrorCause, PluginDescriptionFile> errors)
    {
        super(success, state, errorCause);
        this.uninstalledPlugins = uninstalledPlugins;
        this.disabledPlugins = disabledPlugins;
        this.unloadedPlugins = unloadedPlugins;
        this.errors = errors;
    }
}
