package net.kunmc.lab.kpm.task.tasks.garbage.clean.signal;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.kpm.signal.Signal;
import org.jetbrains.annotations.NotNull;

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
