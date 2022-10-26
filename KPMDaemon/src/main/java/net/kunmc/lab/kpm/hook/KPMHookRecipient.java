package net.kunmc.lab.kpm.hook;

import lombok.AccessLevel;
import lombok.Getter;
import net.kunmc.lab.kpm.KPMDaemon;

import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * KPMフックを受け取るためのインターフェースです。
 * このクラスを継承し、KPMフックを受け取るクラスであることを宣言します。
 */
public abstract class KPMHookRecipient
{
    /**
     * KPMデーモンのインスタンスです。
     */
    @Getter(AccessLevel.PROTECTED)
    private final KPMDaemon daemon;

    private final HashMap<Method, Class<? extends KPMHook>> hooks;

    public KPMHookRecipient(KPMDaemon daemon)
    {
        this.daemon = daemon;
        this.hooks = new HashMap<>();

        this.bakeAll();
    }

    @SuppressWarnings("unchecked")
    private void bakeAll()
    {
        for (Method method : this.getClass().getDeclaredMethods())
        {
            if (method.isAnnotationPresent(HookListener.class))
            {
                if (!(method.getParameterCount() == 1 && KPMHook.class.isAssignableFrom(method.getParameterTypes()[0])))
                    throw new WrongMethodTypeException("Invalid method signature for hook listener: " +
                            method.getName() + " in " + this.getClass().getName());

                this.hooks.put(method, (Class<? extends KPMHook>) method.getParameterTypes()[0]);
            }
        }
    }

    public Method getHookListener(Class<? extends KPMHook> hook)
    {
        for (Method method : this.hooks.keySet())
        {
            Class<? extends KPMHook> hookClass = this.hooks.get(method);
            if (hookClass.isAssignableFrom(hook))
                return method;
        }

        return null;
    }
}
