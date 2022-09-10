package net.kunmc.lab.teamkunpluginmanager.plugin.signal;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallerSignal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SignalHandlerList<T extends InstallerSignal>
{
    @NotNull
    private final Class<T> signalType;

    @NotNull
    private final List<Pair<@Nullable Object, @NotNull Method>> handlers;

    SignalHandlerList(@NotNull Class<T> signalType)
    {
        this.signalType = signalType;

        this.handlers = new ArrayList<>();
    }

    private boolean isBaked(Method method)
    {
        return handlers.stream().parallel()
                .map(Pair::getRight)
                .anyMatch(method::equals);
    }

    public void bakeAll(Object object)
    {
        synchronized (handlers)
        {
            Arrays.stream(object.getClass().getMethods()).parallel()
                    .filter(method -> method.isAnnotationPresent(SignalHandler.class))
                    .filter(method -> !this.isBaked(method))
                    .filter(method -> method.getParameterCount() == 2)
                    .filter(method -> signalType.isAssignableFrom(method.getParameterTypes()[1]))
                    .forEach(method -> {
                        method.setAccessible(true);

                        if (Modifier.isStatic(method.getModifiers()))
                            handlers.add(new Pair<>(null, method));
                        else
                            handlers.add(new Pair<>(object, method));
                    });
        }
    }

    void onSignal(InstallProgress<?, ?> installProgress, T signal)
    {
        synchronized (handlers)
        {
            handlers.forEach(pair -> {
                try
                {
                    pair.getRight().invoke(pair.getLeft(), installProgress, signal);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
    }

    boolean isSignalType(Class<?> type)
    {
        return signalType.isAssignableFrom(type);
    }
}
