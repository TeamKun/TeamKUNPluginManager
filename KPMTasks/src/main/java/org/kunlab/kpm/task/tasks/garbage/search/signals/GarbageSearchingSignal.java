package org.kunlab.kpm.task.tasks.garbage.search.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kunlab.kpm.signal.Signal;

import java.nio.file.Path;

/**
 * 不要なデータの検索を開始したことを示すシグナルです。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GarbageSearchingSignal extends Signal
{
    /**
     * 検索対象のディレクトリです。
     */
    private final Path dataFolder;
}
