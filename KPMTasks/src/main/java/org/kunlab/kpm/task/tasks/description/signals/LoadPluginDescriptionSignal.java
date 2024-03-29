package org.kunlab.kpm.task.tasks.description.signals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

import java.nio.file.Path;

/**
 * プラグイン情報ファイルを読み込むときに発生するシグナルです。
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoadPluginDescriptionSignal extends Signal
{
    /**
     * プラグイン情報ファイルのパスです。
     * これを変更すると、読み込まれるプラグインを変更できます。
     */
    @NotNull
    private Path pluginFile;
}
