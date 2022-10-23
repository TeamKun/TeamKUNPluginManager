package net.kunmc.lab.kpm.installer.signals;

import lombok.Data;
import net.kunmc.lab.kpm.signal.Signal;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * プラグインのKPM情報ファイルが不正な場合に投げられるシグナルです。
 */
@Data
public class InvalidKPMInfoFileSignal implements Signal
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
