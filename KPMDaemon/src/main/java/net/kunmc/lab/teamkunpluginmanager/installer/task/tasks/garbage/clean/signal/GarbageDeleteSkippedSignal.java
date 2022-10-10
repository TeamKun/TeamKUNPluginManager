package net.kunmc.lab.teamkunpluginmanager.installer.task.tasks.garbage.clean.signal;

import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.signal.Signal;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * 不要データの削除がスキップされたことを示すシグナルです。
 */
@Value
public class GarbageDeleteSkippedSignal implements Signal
{
    /**
     * スキップされた不要データのパスです。
     */
    @NotNull
    Path garbageData;
}
