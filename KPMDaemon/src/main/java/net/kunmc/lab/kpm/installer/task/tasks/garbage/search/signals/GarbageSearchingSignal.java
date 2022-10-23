package net.kunmc.lab.kpm.installer.task.tasks.garbage.search.signals;

import lombok.Data;
import net.kunmc.lab.kpm.signal.Signal;

import java.nio.file.Path;

/**
 * 不要データの検索を開始したことを示すシグナルです。
 */
@Data
public class GarbageSearchingSignal implements Signal
{
    /**
     * 検索対象のディレクトリです。
     */
    private final Path dataFolder;
}
