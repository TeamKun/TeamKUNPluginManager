package org.kunlab.kpm.task.tasks.alias.update.signals;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.signal.Signal;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AliasUpdateSignal extends Signal
{
    @NotNull
    private final String source;
    @NotNull
    private final URL sourceURL;
    @NotNull
    private final String name;

    private boolean skip;
    private String alias;

    public AliasUpdateSignal(@NotNull String source, @NotNull URI sourceURI, @NotNull String name,
                             @NotNull String alias)
    {
        this.source = source;
        this.name = name;

        this.skip = false;
        this.alias = alias;

        try
        {
            this.sourceURL = sourceURI.toURL();
        }
        catch (MalformedURLException e)
        {
            throw new UncheckedIOException("Invalid source URI", e);
        }
    }
}
