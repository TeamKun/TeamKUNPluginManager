package net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase;

import net.kunmc.lab.teamkunpluginmanager.plugin.AbstractInstaller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

public class PhaseSubmitter<
        T extends PhaseArgument,
        P extends Enum<P>,
        I extends AbstractInstaller<?, P>,
        A extends PhaseArgument,
        R extends PhaseResult<?, ?>,
        E extends InstallPhase<A, R>>
{
    @NotNull
    private final I installer;
    @NotNull
    private final E phase;
    @NotNull
    private final P phaseState;

    @NotNull
    private final PhaseSubmitter<T, P, I, ?, ?, ?> first;
    @Nullable
    private PhaseSubmitter<T, P, I, ?, ?, ?> next;

    public PhaseSubmitter(@NotNull P phaseState, @NotNull I installer, @NotNull E phase)
    {
        this(phaseState, installer, phase, null, null);
    }

    private PhaseSubmitter(@NotNull P phaseState, @NotNull I installer, @NotNull E phase,
                           @Nullable PhaseSubmitter<T, P, I, ?, ?, ?> first, @Nullable PhaseSubmitter<T, P, I, ?, ?, ?> next)
    {
        this.installer = installer;
        this.phase = phase;
        this.phaseState = phaseState;

        this.next = next;

        if (first == null)
            this.first = this;
        else
            this.first = first;
    }

    public <PA extends PhaseArgument, PR extends PhaseResult<?, ?>, PE extends InstallPhase<PA, PR>>
    @NotNull PhaseSubmitter<T, P, I, PA, PR, PE> then(@NotNull P phaseState, @NotNull PE phase)
    {
        PhaseSubmitter<T, P, I, PA, PR, PE> submitter = new PhaseSubmitter<>(phaseState, installer, phase, this.first, null);
        this.next = submitter;

        return submitter;
    }

    public @NotNull R submitOnce(@NotNull A argument)
    {
        return this.phase.runPhase(argument);
    }

    @SuppressWarnings("unchecked")
    protected <PA extends PhaseArgument> @NotNull R submitOnceUnsafe(@NotNull PA argument)
    {
        return this.phase.runPhase((A) argument);
    }

    @SuppressWarnings("unchecked")
    private static <T extends InstallPhase<?, ?>> Class<? extends PhaseArgument> getPhaseArgumentOf(@NotNull T phase)
    {
        return (Class<? extends PhaseArgument>) ((ParameterizedType) phase.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    private static Constructor<PhaseArgument> getConstructor(@NotNull Class<? extends PhaseArgument> clazz,
                                                             @NotNull PhaseResult<?, ?> parentResult)
    {

        try
        {
            for (Constructor<?> constructor : clazz.getConstructors())
            {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(parentResult.getClass()))
                {
                    constructor.setAccessible(true);
                    return (Constructor<PhaseArgument>) constructor;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        throw new IllegalArgumentException("No constructor found for " + clazz.getName());
    }

    public @NotNull PhaseResult<?, ?> submit(@NotNull T firstArgument)
    {
        PhaseSubmitter<T, P, I, ?, ?, ?> submitter = this.first;

        installer.getProgress().setPhase(phaseState);
        PhaseResult<?, ?> result = submitter.submitOnceUnsafe(firstArgument);

        try
        {
            while (submitter.next != null)
            {
                if (!result.isSuccess())
                    return result;

                submitter = submitter.next;

                installer.getProgress().setPhase(submitter.phaseState);
                PhaseArgument nextArgument = getConstructor(getPhaseArgumentOf(submitter.phase), result).newInstance(result);

                result = submitter.submitOnceUnsafe(nextArgument);

            }
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
