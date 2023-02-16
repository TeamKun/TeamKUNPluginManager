package org.kunlab.kpm.hook;

import lombok.Getter;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.interfaces.hook.KPMHook;
import org.kunlab.kpm.interfaces.hook.KPMHookRecipient;

import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.util.HashMap;

public abstract class KPMHookRecipientBase implements KPMHookRecipient
{
    @Getter
    private final KPMRegistry registry;

    private final HashMap<Method, Class<? extends KPMHook>> hooks;

    public KPMHookRecipientBase(KPMRegistry registry)
    {
        this.registry = registry;
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

    @Override
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
