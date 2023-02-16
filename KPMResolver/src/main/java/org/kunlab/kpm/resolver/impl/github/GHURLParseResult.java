package org.kunlab.kpm.resolver.impl.github;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
class GHURLParseResult
{
    @NotNull
    String owner;
    @NotNull
    String repositoryName;
    @NotNull
    String repository;
    @Nullable
    String tag;
    @Nullable
    String finalName;
}
