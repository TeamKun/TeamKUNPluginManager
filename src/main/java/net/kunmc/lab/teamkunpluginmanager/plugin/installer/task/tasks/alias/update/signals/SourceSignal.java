package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.alias.update.signals;

import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

@Data
abstract class SourceSignal implements Signal
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
