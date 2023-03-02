package org.kunlab.kpm.task.tasks.install.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

import java.nio.file.Path;

/**
 * プラグインを、ダウンロードの仮ディレクトリからプラグインディレクトリに移動する際に送信されるシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PluginRelocatingSignal extends Signal
{
    /**
     * プラグインのパスです。
     */
    @NotNull
    private final Path source;
    /**
     * 移動先のパスです。
     * デフォルトは {@code  plugins/} です。
     */
    @NotNull
    private Path target;
}
