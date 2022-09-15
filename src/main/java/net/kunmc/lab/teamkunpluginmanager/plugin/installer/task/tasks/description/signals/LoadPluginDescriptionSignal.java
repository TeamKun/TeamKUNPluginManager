package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.description.signals;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * プラグイン情報ファイルを読み込むときに発生するシグナルです。
 */
@Data
@AllArgsConstructor
public class LoadPluginDescriptionSignal implements Signal
{
    /**
     * プラグイン情報ファイルのパスです。
     * これを変更すると、読み込まれるプラグインを変更できます。
     */
    @NotNull
    private Path pluginFile;
}
