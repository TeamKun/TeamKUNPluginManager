package net.kunmc.lab.teamkunpluginmanager.plugin;

import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallFailedInstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallResult;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignalHandler;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.GeneralPhaseErrorCause;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.InstallPhase;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseArgument;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseEnum;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.phase.PhaseResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractInstaller<E extends Enum<E>, P extends Enum<P>>
{
    protected final InstallProgress<P> progress;
    protected final InstallerSignalHandler signalHandler;

    public AbstractInstaller(InstallerSignalHandler signalHandler) throws IOException
    {
        this.progress = new InstallProgress<>(true);
        this.signalHandler = signalHandler;
    }

    public abstract InstallResult<P> execute(@NotNull String query) throws IOException;

    @NotNull
    protected InstallResult<P> success()
    {
        return new InstallResult<>(true, this.progress);
    }

    public <T extends Enum<T> & PhaseEnum> InstallFailedInstallResult<P, T, ?> error(@NotNull T reason)
    {  // TODO: Implement debug mode
        return new InstallFailedInstallResult<>(this.progress, reason);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T> & PhaseEnum, S extends Enum<S> & PhaseEnum> InstallFailedInstallResult<P, T, S> error(
            @NotNull T reason,
            @NotNull PhaseEnum phaseStatus)
    {  // TODO: Implement debug mode
        return new InstallFailedInstallResult<>(this.progress, reason, (S) phaseStatus);
    }

    @NotNull
    protected <T extends Enum<T> & PhaseEnum> InstallResult<P> handlePhaseError(@NotNull PhaseResult<?, T> result)
    {
        if (result.getErrorCause() != null)
            return this.error(result.getErrorCause(), result.getPhase());
        else
            return this.error(GeneralPhaseErrorCause.ILLEGAL_INTERNAL_STATE, result.getPhase());
    }

    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Constructor<? extends PhaseArgument> findConstructor(@NotNull Class<? extends PhaseResult> argumentClass,
                                                                 @NotNull Class<?> phaseClass)
    {
        for (Constructor<?> constructor : phaseClass.getConstructors())
        {
            if (constructor.getParameterCount() == 1
                    && constructor.getParameterTypes()[0].isAssignableFrom(argumentClass))
            {
                constructor.setAccessible(true);
                return (Constructor<? extends PhaseArgument>) constructor;
            }
        }

        throw new IllegalArgumentException("No constructor found for chain element: " + phaseClass.getName());
    }

    @NotNull
    private Class<?> getTypeParameterClass(Class<?> clazz, int index)
    {
        return clazz.getTypeParameters()[index].getClass();
    }

    private <A extends PhaseArgument> PhaseArgument toArgument(@NotNull PhaseResult<?, ?> previousResult,
                                                               @NotNull Class<?> argumentClass)
            throws InvocationTargetException, InstantiationException, IllegalAccessException
    {
        Constructor<? extends PhaseArgument> constructor =
                findConstructor(previousResult.getClass(), argumentClass);

        return constructor.newInstance(previousResult);
    }

    @NotNull
    private PhaseResult<?, ?> submitPhase(@NotNull PhaseArgument phaseArgument,
                                          @NotNull InstallPhase<?, ?> phase)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Method executeMethod = phase.getClass().getMethod("runPhase", PhaseArgument.class);
        executeMethod.setAccessible(true);

        return (PhaseResult<?, ?>) executeMethod.invoke(phase, phaseArgument);
    }

    private PhaseResult<?, ?> submitPhase(PhaseResult<?, ?> phaseArgument, InstallPhase<?, ?> phase)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException
    {
        PhaseArgument argument = toArgument(phaseArgument, getTypeParameterClass(phase.getClass(), 0));

        return submitPhase(argument, phase);
    }

    @Nullable
    protected InstallPhase<?, ?> submitPhase(@NotNull PhaseArgument firstArgument, @NotNull InstallPhase<?, ?>... phases)
    {
        InstallPhase<?, ?> tryingPhase = null; // Return null if no error occurs in all phases.
        PhaseResult<?, ?> phaseResult = null;

        try
        {
            for (InstallPhase<?, ?> phase : phases)
            {
                if (phaseResult != null && !phaseResult.isSuccess())
                    break;

                tryingPhase = phase;
                if (phaseResult == null)
                    phaseResult = submitPhase(firstArgument, phase);
                else
                    phaseResult = submitPhase(phaseResult, phase);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (phaseResult != null && !phaseResult.isSuccess())
            return tryingPhase;
        else
            return null;
    }
}
