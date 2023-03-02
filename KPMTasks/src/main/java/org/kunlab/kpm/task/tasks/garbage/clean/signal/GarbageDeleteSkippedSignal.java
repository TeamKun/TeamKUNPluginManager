package org.kunlab.kpm.task.tasks.garbage.clean.signal;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

import java.nio.file.Path;

/**
 * 不要なデータの削除がスキップされたことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class GarbageDeleteSkippedSignal extends Signal
{
    /**
     * スキップされた不要なデータのパスです。
     */
    @NotNull
    Path garbageData;
}
