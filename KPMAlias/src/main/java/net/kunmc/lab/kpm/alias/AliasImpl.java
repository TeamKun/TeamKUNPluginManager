package net.kunmc.lab.kpm.alias;

import lombok.Value;
import net.kunmc.lab.kpm.interfaces.alias.Alias;
import org.jetbrains.annotations.NotNull;

@Value
class AliasImpl implements Alias
{
    @NotNull
    String alias;
    @NotNull
    String query;
    @NotNull
    String source;

}
