package net.kunmc.lab.kpm.installer.task.tasks.install.signals;

import lombok.Data;
import net.kunmc.lab.kpm.kpminfo.KPMInformationFile;
import net.kunmc.lab.kpm.signal.Signal;
import net.kunmc.lab.kpm.utils.versioning.Version;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

/**
 * プラグインがこのKPMと互換性がない場合に投げられるシグナルです。
 */
@Data
public class PluginIncompatibleWithKPMSignal implements Signal
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
