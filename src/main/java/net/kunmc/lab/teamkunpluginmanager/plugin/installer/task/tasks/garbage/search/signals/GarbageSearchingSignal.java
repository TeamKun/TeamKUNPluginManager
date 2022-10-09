package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.garbage.search.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;

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
