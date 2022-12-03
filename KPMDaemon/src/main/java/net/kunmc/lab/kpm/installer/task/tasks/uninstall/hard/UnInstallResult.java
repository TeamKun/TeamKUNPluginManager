package net.kunmc.lab.kpm.installer.task.tasks.uninstall.hard;

import lombok.Getter;
import net.kunmc.lab.kpm.installer.task.TaskResult;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * プラグインのアンインストールを行うタスクの結果です。
 */
public class UnInstallResult extends TaskResult<UninstallState, UninstallErrorCause>
{
    /**
     * アンインストールされたプラグインのプラグイン情報ファイルです。
     */
    @Getter
    @NotNull
    private final List<PluginDescriptionFile> uninstalledPlugins;
    /**
     * アンインストールに失敗した理由です。
     */
    @Getter
    @NotNull
    private final Map<UninstallErrorCause, PluginDescriptionFile> errors;

    public UnInstallResult(boolean success, @NotNull UninstallState state, @Nullable UninstallErrorCause errorCause, @NotNull List<PluginDescriptionFile> uninstalledPlugins, @NotNull Map<UninstallErrorCause, PluginDescriptionFile> errors)
    {
        super(success, state, errorCause);
        this.uninstalledPlugins = uninstalledPlugins;
        this.errors = errors;
    }
}
