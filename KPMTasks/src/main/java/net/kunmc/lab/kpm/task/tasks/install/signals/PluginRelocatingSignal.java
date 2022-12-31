package net.kunmc.lab.kpm.task.tasks.install.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

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
