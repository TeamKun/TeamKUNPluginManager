package net.kunmc.lab.kpm.alias;

import lombok.Value;
import net.kunmc.lab.kpm.interfaces.alias.AliasSource;
import org.jetbrains.annotations.NotNull;

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
