package org.kunlab.kpm.alias;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.kpm.alias.interfaces.AliasSource;

@Value
class AliasSourceImpl implements AliasSource
{
    @NotNull
    String name;
    @NotNull
    String source;

    @NotNull
    AliasSourceType type;
}
