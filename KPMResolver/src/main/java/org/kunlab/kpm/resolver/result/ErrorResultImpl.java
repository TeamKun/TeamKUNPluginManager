package org.kunlab.kpm.resolver.result;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.resolver.ErrorCause;
import org.kunlab.kpm.resolver.interfaces.BaseResolver;
import org.kunlab.kpm.resolver.interfaces.result.ErrorResult;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class ErrorResultImpl implements ErrorResult
{
    @Nullable
    private final BaseResolver resolver;
    @NotNull
    private final ErrorCause cause;
    @NotNull
    private final ResolveResult.Source source;
    @Nullable
    private final String message;

    public ErrorResultImpl(@Nullable BaseResolver resolver, @NotNull ErrorCause cause, @NotNull Source source)
    {
        this(resolver, cause, source, null);
    }

}
