package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.alias.update.signals;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.Signal;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AliasUpdateSignal implements Signal
{
    @NotNull
    private final String source;
    @NotNull
    private final URL sourceURL;
    @NotNull
    private final String name;

    private boolean skip;
    private String alias;

    public AliasUpdateSignal(@NotNull String source, @NotNull URL sourceURL, @NotNull String name,
                             @NotNull String alias)
    {
        this.source = source;
        this.sourceURL = sourceURL;
        this.name = name;

        this.skip = false;
        this.alias = alias;
    }
}
