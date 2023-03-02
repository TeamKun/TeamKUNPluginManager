package org.kunlab.kpm.task.tasks.install.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.kpminfo.KPMInformationFile;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.versioning.Version;

/**
 * プラグインがこのKPMと互換性がない場合に投げられるシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginIncompatibleWithKPMSignal extends Signal
{
    /**
     * プラグインの説明ファイルです。
     */
    @NotNull
    private final PluginDescriptionFile pluginDescription;
    /**
     * プラグインのKPM情報です。
     */
    @NotNull
    private final KPMInformationFile kpmInformation;
    /**
     * このKPMのバージョンです。
     */
    @NotNull
    private final Version kpmVersion;

    /**
     * 強制的にインストールするかどうかを示すフラグです。
     */
    private boolean forceInstall;

    public PluginIncompatibleWithKPMSignal(@NotNull PluginDescriptionFile pluginDescription, @NotNull KPMInformationFile kpmInformation, @NotNull Version kpmVersion)
    {
        this.pluginDescription = pluginDescription;
        this.kpmInformation = kpmInformation;
        this.kpmVersion = kpmVersion;

        this.forceInstall = false;
    }
}
