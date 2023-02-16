package org.kunlab.kpm.hook;

import lombok.Getter;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.interfaces.hook.HookExecutor;
import org.kunlab.kpm.interfaces.hook.HookRecipientList;
import org.kunlab.kpm.interfaces.hook.KPMHook;
import org.kunlab.kpm.interfaces.hook.KPMHookRecipient;

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
            e.printStackTrace();
        }
    }

    @Override
    public void runHook(HookRecipientList recipients, KPMHook hook)
    {
        for (KPMHookRecipient recipient : recipients)
            this.runHook(recipient, hook);
    }
}
