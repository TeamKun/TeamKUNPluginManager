package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.phases.predicate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class PredicateArgument implements PhaseArgument
{
    private final Object arg1;
    private final Object arg2;
    private final Object arg3;
    private final Object arg4;
    private final Object arg5;
    private final Object arg6;
    private final Object arg7;
    private final Object arg8;
    private final Object arg9;
    private final Object arg10;

    public static PredicateArgument of(@NotNull Object arg1)
    {
        return new PredicateArgument(arg1, null, null, null, null, null, null, null, null, null);
    }

    public static PredicateArgument of(@NotNull Object arg1, @NotNull Object arg2)
    {
        return new PredicateArgument(arg1, arg2, null, null, null, null, null, null, null, null);
    }

    public static PredicateArgument of(@NotNull Object arg1, @NotNull Object arg2, @NotNull Object arg3)
    {
        return new PredicateArgument(arg1, arg2, arg3, null, null, null, null, null, null, null);
    }

    public static PredicateArgument of(@NotNull Object arg1, @NotNull Object arg2, @NotNull Object arg3,
                                       @NotNull Object arg4)
    {
        return new PredicateArgument(arg1, arg2, arg3, arg4, null, null, null, null, null, null);
    }

    public static PredicateArgument of(@NotNull Object arg1, @NotNull Object arg2, @NotNull Object arg3,
                                       @NotNull Object arg4, @NotNull Object arg5)
    {
        return new PredicateArgument(arg1, arg2, arg3, arg4, arg5, null, null, null, null, null);
    }

    public static PredicateArgument of(@NotNull Object arg1, @NotNull Object arg2, @NotNull Object arg3,
                                       @NotNull Object arg4, @NotNull Object arg5, @NotNull Object arg6)
    {
        return new PredicateArgument(arg1, arg2, arg3, arg4, arg5, arg6, null, null, null, null);
    }

    public static PredicateArgument of(@NotNull Object arg1, @NotNull Object arg2, @NotNull Object arg3,
                                       @NotNull Object arg4, @NotNull Object arg5, @NotNull Object arg6, @NotNull Object arg7)
    {
        return new PredicateArgument(arg1, arg2, arg3, arg4, arg5, arg6, arg7, null, null, null);
    }

    public static PredicateArgument of(@NotNull Object arg1, @NotNull Object arg2, @NotNull Object arg3,
                                       @NotNull Object arg4, @NotNull Object arg5, @NotNull Object arg6,
                                       @NotNull Object arg7, @NotNull Object arg8)
    {
        return new PredicateArgument(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, null, null);
    }

    public static PredicateArgument of(@NotNull Object arg1, @NotNull Object arg2, @NotNull Object arg3,
                                       @NotNull Object arg4, @NotNull Object arg5, @NotNull Object arg6,
                                       @NotNull Object arg7, @NotNull Object arg8, @NotNull Object arg9)
    {
        return new PredicateArgument(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, null);
    }

    public static PredicateArgument of(@NotNull Object arg1, @NotNull Object arg2, @NotNull Object arg3,
                                       @NotNull Object arg4, @NotNull Object arg5, @NotNull Object arg6,
                                       @NotNull Object arg7, @NotNull Object arg8, @NotNull Object arg9,
                                       @NotNull Object arg10)
    {
        return new PredicateArgument(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
    }
}
