package org.kunlab.kpm.task.tasks.alias.update.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.signal.Signal;

import java.nio.file.Path;

@Data
@EqualsAndHashCode(callSuper = false)
abstract class SourceSignal extends Signal
{
    /**
     * ソースの名前です。
     */
    @NotNull
    private final String sourceName;
    /**
     * ソースファイルのパスです。
     */
    @NotNull
    private final Path sourcePath;
    /**
     * ソースファイルのURLです。
     */
    @Nullable
    private final String sourceURL;
}
