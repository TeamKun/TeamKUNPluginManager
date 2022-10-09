package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.garbage.clean.signal;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;

import java.nio.file.Path;
import java.util.List;

/**
 * 不要なデータが列挙されたことを示すシグナルです。
 */
@Data
public class GarbageEnumeratedSignal implements Signal
{
    /**
     * 不要なデータのパスのリストです。
     */
    private final List<Path> garbageDatas;
    /**
     * 削除をキャンセルするかどうかです。
     */
    private boolean cancel;
}
