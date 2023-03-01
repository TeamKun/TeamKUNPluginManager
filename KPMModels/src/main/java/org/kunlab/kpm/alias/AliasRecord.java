package org.kunlab.kpm.alias;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.interfaces.alias.Alias;

@Value
class AliasRecord implements Alias
{
    @NotNull
    String alias;
    @NotNull
    String query;
    @NotNull
    String source;

}