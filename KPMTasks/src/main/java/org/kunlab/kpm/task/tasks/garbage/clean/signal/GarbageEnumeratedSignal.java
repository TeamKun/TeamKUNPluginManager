package org.kunlab.kpm.task.tasks.garbage.clean.signal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kunlab.kpm.signal.Signal;

import java.nio.file.Path;
import java.util.List;

/**
 * 不要なデータが列挙されたことを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GarbageEnumeratedSignal extends Signal
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
