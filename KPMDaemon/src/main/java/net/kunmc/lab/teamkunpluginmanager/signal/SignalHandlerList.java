package net.kunmc.lab.teamkunpluginmanager.signal;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.installer.InstallProgress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SignalHandlerList<T extends Signal>
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
        return this.handlers.stream().parallel()
                .map(Pair::getRight)
                .anyMatch(method::equals);
    }

    public void bakeAll(Object object)
    {
        synchronized (this.handlers)
        {
            Arrays.stream(object.getClass().getMethods()).parallel()
                    .filter(method -> method.isAnnotationPresent(SignalHandler.class))
                    .filter(method -> !this.isBaked(method))
                    .filter(method -> method.getParameterCount() == 1)
                    .filter(method -> this.signalType.isAssignableFrom(method.getParameterTypes()[0]))
                    .forEach(method -> {
                        method.setAccessible(true);

                        if (Modifier.isStatic(method.getModifiers()))
                            this.handlers.add(new Pair<>(null, method));
                        else
                            this.handlers.add(new Pair<>(object, method));
                    });
        }
    }

    void onSignal(InstallProgress<?, ?> installProgress, T signal)
    {
        synchronized (this.handlers)
        {
            this.handlers.forEach(pair -> {
                try
                {
                    pair.getRight().invoke(pair.getLeft(), signal);
                }
                catch (Exception e)
                {
                    System.out.println("Target: " + pair.getRight().getDeclaringClass().getName() + "#" + pair.getRight().getName());
                    e.printStackTrace();
                }
            });
        }
    }

    boolean isSignalType(Class<?> type)
    {
        return this.signalType.isAssignableFrom(type);
    }
}
