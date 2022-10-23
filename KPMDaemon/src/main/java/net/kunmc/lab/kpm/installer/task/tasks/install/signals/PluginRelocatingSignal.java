package net.kunmc.lab.kpm.installer.task.tasks.install.signals;

import lombok.Data;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * プラグインを、ダウンロードの仮ディレクトリからプラグインディレクトリに移動する際に送信されるシグナルです。
 */
@Data
public class PluginRelocatingSignal implements Signal
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
