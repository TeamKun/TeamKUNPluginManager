package org.kunlab.kpm.installer.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

import java.nio.file.Path;

/**
 * プラグインのKPM情報ファイルが不正な場合に投げられるシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class InvalidKPMInfoFileSignal extends Signal
{
    /**
     * プラグインの名前です。
     */
    @NotNull
    private final Path plugin;
    /**
     * プラグインの {@link org.bukkit.plugin.PluginDescriptionFile} です。
     */
    @NotNull
    private final PluginDescriptionFile descriptionFile;

    /**
     * プラグインのKPM情報ファイルを無視するかどうかを示すフラグです。
     * {@code true} の場合、プラグインの持つKPM情報ファイルは無視されます。
     * {@code false} の場合、KPM情報ファイルが不正なため、プラグインのインストールが中断される可能性があります。
     */
    private boolean ignore;

    public InvalidKPMInfoFileSignal(@NotNull Path plugin, @NotNull PluginDescriptionFile descriptionFile)
    {
        this.plugin = plugin;
        this.descriptionFile = descriptionFile;
        this.ignore = false;
    }
}
