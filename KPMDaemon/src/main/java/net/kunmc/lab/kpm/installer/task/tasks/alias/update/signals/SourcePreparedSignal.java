package net.kunmc.lab.kpm.installer.task.tasks.alias.update.signals;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * ソースが準備されたことを示すシグナルです。
 */
@Getter
@Setter
public class SourcePreparedSignal extends SourceSignal
{
    private boolean skip;

    public SourcePreparedSignal(@NotNull String sourceName, @NotNull Path sourcePath, @Nullable String sourceURL)
    {
        super(sourceName, sourcePath, sourceURL);

        this.skip = true;
    }
}
