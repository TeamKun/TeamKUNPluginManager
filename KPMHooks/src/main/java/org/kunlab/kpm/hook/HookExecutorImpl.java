package org.kunlab.kpm.hook;

import lombok.Getter;
import org.kunlab.kpm.hook.interfaces.HookExecutor;
import org.kunlab.kpm.hook.interfaces.HookRecipientList;
import org.kunlab.kpm.hook.interfaces.KPMHook;
import org.kunlab.kpm.hook.interfaces.KPMHookRecipient;
import org.kunlab.kpm.interfaces.KPMRegistry;

import java.lang.reflect.Method;

public class HookExecutorImpl implements HookExecutor
{
    @Getter
    private final KPMRegistry registry;

    public HookExecutorImpl(KPMRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public void runHook(KPMHookRecipient recipient, KPMHook hook)
    {
        Method hookMethod = recipient.getHookListener(hook.getClass());

        if (hookMethod == null)
            throw new IllegalStateException("Hook method not found: " + hook.getClass().getName());

        try
        {
            hookMethod.invoke(recipient, hook);
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
        }
    }

    @Override
    public void runHook(HookRecipientList recipients, KPMHook hook)
    {
        for (KPMHookRecipient recipient : recipients)
            this.runHook(recipient, hook);
    }
}
