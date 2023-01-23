package net.kunmc.lab.plugin.kpmupgrader.mocks;

import lombok.AllArgsConstructor;
import net.kunmc.lab.kpm.interfaces.KPMRegistry;
import net.kunmc.lab.kpm.interfaces.hook.HookExecutor;
import net.kunmc.lab.kpm.interfaces.hook.HookRecipientList;
import net.kunmc.lab.kpm.interfaces.hook.KPMHook;
import net.kunmc.lab.kpm.interfaces.hook.KPMHookRecipient;

@AllArgsConstructor
public class HookExecutorMock implements HookExecutor
{
    private final KPMRegistry registry;

    @Override
    public void runHook(KPMHookRecipient recipient, KPMHook hook)
    {

    }

    @Override
    public void runHook(HookRecipientList recipients, KPMHook hook)
    {

    }

    @Override
    public KPMRegistry getRegistry()
    {
        return this.registry;
    }
}
